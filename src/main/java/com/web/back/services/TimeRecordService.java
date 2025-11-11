package com.web.back.services;

import com.web.back.model.dto.TimeRecordDto;
import com.web.back.model.entities.EmployeeTimesheet;
import com.web.back.model.entities.TimeRecord;
import com.web.back.model.entities.Timesheet;
import com.web.back.model.requests.TimeRecordGeneralRequest;
import com.web.back.model.requests.TimeRecordRequest;
import com.web.back.repositories.EmployeeRepository;
import com.web.back.repositories.EmployeeTimesheetsRepository;
import com.web.back.repositories.TimeRecordRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class TimeRecordService {
    private final TimeRecordRepository timeRecordRepository;
    private final EmployeeRepository employeeRepository;
    private final EmployeeTimesheetsRepository employeeTimesheetsRepository;

    public TimeRecordService(TimeRecordRepository timeRecordRepository,
                             EmployeeRepository employeeRepository,
                             EmployeeTimesheetsRepository employeeTimesheetsRepository) {
        this.timeRecordRepository = timeRecordRepository;
        this.employeeRepository = employeeRepository;
        this.employeeTimesheetsRepository = employeeTimesheetsRepository;
    }

    @Transactional
    public TimeRecordDto create(TimeRecordRequest request) {
        var timeRule = mapToEntity(request, null);
        var saved = timeRecordRepository.save(timeRule);

        return mapToDto(saved);
    }

    @Transactional
    public List<TimeRecordDto> readAll() {
        return timeRecordRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    @Transactional
    public TimeRecordDto readById(String id) {
        var entity = timeRecordRepository.findById(UUID.fromString(id));

        return entity
                .map(this::mapToDto)
                .orElse(null);

    }

    @Transactional
    public TimeRecordDto update(String id, TimeRecordRequest request) {
        var exitingEntity = timeRecordRepository.findById(UUID.fromString(id));

        if (exitingEntity.isEmpty()) {
            throw new RuntimeException("TimeRecord not found");
        }

        var timeRule = mapToEntity(request, exitingEntity.get().getId());
        var saved = timeRecordRepository.save(timeRule);

        return mapToDto(saved);
    }

    public void deleteById(String id) {
        timeRecordRepository.deleteById(UUID.fromString(id));
    }

    @Transactional
    public void registerTimeMarks(List<TimeRecordGeneralRequest> timeRecordMarks) {
        var timeMarksGroupedByEmployee = timeRecordMarks.stream()
                .collect(java.util.stream.Collectors.groupingBy(TimeRecordGeneralRequest::employeeNumber));

        for (var entry : timeMarksGroupedByEmployee.entrySet()) {
            var marksForEmployee = entry.getValue();
            var employeeTimesheets = employeeTimesheetsRepository.findAllByEmployeeNumEmployee(entry.getKey());
            var sortedMarks = marksForEmployee.stream()
                    .sorted(Comparator.comparing(TimeRecordGeneralRequest::markDateTime))
                    .toList();

            for (var mark : sortedMarks) {
                var existentTimeRecords = timeRecordRepository.findAllByDateAndEmployee_NumEmployee(mark.date(), entry.getKey());

                if (!existentTimeRecords.isEmpty() && isFirstTurnComplete(existentTimeRecords)) {
                    var nextAvailableTimeRecord = getOrCreateNextTurnTimeRecord(existentTimeRecords, entry.getKey(), mark.date());
                    setNextAvailableTime(nextAvailableTimeRecord, mark.time());
                    timeRecordRepository.save(nextAvailableTimeRecord);
                    continue;
                }

                var timesheetForDayBefore = getTimesheetForOffset(employeeTimesheets, mark.date(), -1);
                var timesheetForSameDay = getTimesheetForOffset(employeeTimesheets, mark.date(), 0);

                if (timesheetForDayBefore == null && timesheetForSameDay == null) {
                    var nextAvailableTimeRecord = getOrCreateFirstTurnTimeRecord(existentTimeRecords, entry.getKey(), mark.date());
                    setNextAvailableTime(nextAvailableTimeRecord, mark.time());
                    timeRecordRepository.save(nextAvailableTimeRecord);
                    continue;
                }

                var eventTimeline = buildEventTimeline(timesheetForDayBefore, timesheetForSameDay, mark.date());
                var closestEventType = getClosestEventType(eventTimeline, mark.markDateTime());
                if (closestEventType == null) continue;

                if(timesheetForSameDay == null && closestEventType.startsWith("oneDayBeforeDeparture")){
                    var timeRecords = timeRecordRepository.findAllByDateAndEmployee_NumEmployee(mark.date().minusDays(1), entry.getKey());
                    var allPreviousTurnsComplete = timeRecords.stream()
                            .allMatch(tr -> tr.getDepartureTime() != null);

                    if(allPreviousTurnsComplete) {
                        var nextAvailableTimeRecord = getOrCreateFirstTurnTimeRecord(existentTimeRecords, entry.getKey(), mark.date());
                        setNextAvailableTime(nextAvailableTimeRecord, mark.time());
                        timeRecordRepository.save(nextAvailableTimeRecord);
                        continue;
                    }
                }

                LocalDate queryDate = closestEventType.startsWith("sameDay")
                        ? mark.date()
                        : mark.date().minusDays(1);

                var timeRecord = timeRecordRepository.findByDateAndEmployee_NumEmployeeAndTurn(queryDate, entry.getKey(), 1);
                if (timeRecord == null) {
                    timeRecord = createTimeRecord(entry.getKey(), queryDate, 1);
                    if (timeRecord == null) continue;
                }

                setTimeFieldByEventType(timeRecord, closestEventType, mark.time());
                timeRecordRepository.save(timeRecord);
            }
        }
    }

    private boolean isFirstTurnComplete(List<TimeRecord> records) {
        return records.stream()
                .filter(tr -> tr.getTurn() == 1)
                .anyMatch(tr -> tr.getDepartureTime() != null);
    }

    private TimeRecord getOrCreateNextTurnTimeRecord(List<TimeRecord> records, String employeeNum, LocalDate date) {
        var nextAvailable = records.stream()
                .filter(tr -> tr.getTurn() > 1 && tr.getDepartureTime() == null)
                .max(Comparator.comparing(TimeRecord::getTurn))
                .orElse(null);

        if (nextAvailable == null) {
            int nextTurnNumber = records.stream().mapToInt(TimeRecord::getTurn).max().orElse(1) + 1;
            nextAvailable = createTimeRecord(employeeNum, date, nextTurnNumber);
        }
        return nextAvailable;
    }

    private TimeRecord getOrCreateFirstTurnTimeRecord(List<TimeRecord> records, String employeeNum, LocalDate date) {
        var firstTurn = records.stream()
                .filter(tr -> tr.getTurn() == 1)
                .findFirst()
                .orElse(null);

        if (firstTurn == null) {
            firstTurn = createTimeRecord(employeeNum, date, 1);
        }
        return firstTurn;
    }

    private TimeRecord createTimeRecord(String employeeNum, LocalDate date, int turn) {
        var employee = employeeRepository.findByNumEmployee(employeeNum).orElse(null);
        if (employee == null) return null;
        var tr = new TimeRecord();
        tr.setEmployee(employee);
        tr.setDate(date);
        tr.setTurn(turn);
        return tr;
    }

    private void setNextAvailableTime(TimeRecord tr, LocalTime time) {
        if (tr.getEntryTime() == null) tr.setEntryTime(time);
        else if (tr.getBreakDepartureTime() == null) tr.setBreakDepartureTime(time);
        else if (tr.getBreakReturnTime() == null) tr.setBreakReturnTime(time);
        else if (tr.getDepartureTime() == null) tr.setDepartureTime(time);
    }

    private Timesheet getTimesheetForOffset(List<EmployeeTimesheet> employeeTimesheets, LocalDate date, int offset) {
        LocalDate targetDate = date.plusDays(offset);
        return employeeTimesheets.stream()
                .filter(ts -> {
                    var tsStart = LocalDateTime.of(ts.getFromDate(), LocalTime.MIN);
                    var tsEnd = LocalDateTime.of(ts.getToDate(), LocalTime.MAX);
                    return !targetDate.isBefore(tsStart.toLocalDate()) && !targetDate.isAfter(tsEnd.toLocalDate());
                })
                .map(EmployeeTimesheet::getTimesheet)
                .findFirst()
                .orElse(null);
    }

    private Map<String, LocalDateTime> buildEventTimeline(Timesheet before, Timesheet same, LocalDate date) {
        Map<String, LocalDateTime> timeline = new java.util.HashMap<>();
        if (before != null) {
            timeline.put("oneDayBeforeEntry", LocalDateTime.of(date.minusDays(1), before.getEntryTime()));
            timeline.put("oneDayBeforeDeparture",
                    before.getDepartureTime().isBefore(before.getEntryTime())
                            ? LocalDateTime.of(date, before.getDepartureTime())
                            : LocalDateTime.of(date.minusDays(1), before.getDepartureTime()));

            if(before.getBreakDepartureTime() != null){
                timeline.put("oneDayBeforeBreakDeparture", LocalDateTime.of(date.minusDays(1), before.getBreakDepartureTime()));
            }

            if(before.getBreakReturnTime() != null){
                timeline.put("oneDayBeforeBreakReturn",
                        before.getBreakDepartureTime() != null && before.getBreakReturnTime().isBefore(before.getBreakDepartureTime())
                                ? LocalDateTime.of(date, before.getBreakReturnTime())
                                : LocalDateTime.of(date.minusDays(1), before.getBreakReturnTime()));
            }
        }
        if (same != null) {
            timeline.put("sameDayEntry", LocalDateTime.of(date, same.getEntryTime()));
            timeline.put("sameDayDeparture",
                    same.getDepartureTime().isBefore(same.getEntryTime())
                            ? LocalDateTime.of(date.plusDays(1), same.getDepartureTime())
                            : LocalDateTime.of(date, same.getDepartureTime()));

            if(same.getBreakDepartureTime() != null){
                timeline.put("sameDayBreakDeparture", LocalDateTime.of(date, same.getBreakDepartureTime()));
            }

            if(same.getBreakReturnTime() != null){
                timeline.put("sameDayBreakReturn",
                        same.getBreakDepartureTime() != null && same.getBreakReturnTime().isBefore(same.getBreakDepartureTime())
                                ? LocalDateTime.of(date.plusDays(1), same.getBreakReturnTime())
                                : LocalDateTime.of(date, same.getBreakReturnTime())
                );
            }
        }
        return timeline;
    }

    private String getClosestEventType(Map<String, LocalDateTime> timeline, LocalDateTime markDateTime) {
        return timeline.entrySet().stream()
                .min(Comparator.comparing(e -> Duration.between(markDateTime, e.getValue()).abs()))
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    private void setTimeFieldByEventType(TimeRecord tr, String eventType, LocalTime time) {
        switch (eventType) {
            case "oneDayBeforeEntry", "sameDayEntry" -> tr.setEntryTime(time);
            case "oneDayBeforeDeparture", "sameDayDeparture" -> tr.setDepartureTime(time);
            case "oneDayBeforeBreakDeparture", "sameDayBreakDeparture" -> tr.setBreakDepartureTime(time);
            case "oneDayBeforeBreakReturn", "sameDayBreakReturn" -> tr.setBreakReturnTime(time);
        }
    }

    private TimeRecord mapToEntity(TimeRecordRequest request, UUID id) {
        var entity = new TimeRecord();
        if (id != null) entity.setId(id);

        var employee = employeeRepository.getReferenceById(UUID.fromString(request.employeeId()));

        entity.setEmployee(employee);
        entity.setTurn(request.turn());
        entity.setEntryTime(request.entryTime());
        entity.setBreakDepartureTime(request.breakDepartureTime());
        entity.setBreakReturnTime(request.breakReturnTime());
        entity.setDepartureTime(request.departureTime());
        entity.setDate(request.date());
        return entity;
    }

    private TimeRecordDto mapToDto(TimeRecord entity) {
        return new TimeRecordDto(
                entity.getId().toString(),
                entity.getEmployeeId().toString(),
                entity.getEmployee().getNumEmployee(),
                entity.getTurn(),
                entity.getEntryTime(),
                entity.getBreakDepartureTime(),
                entity.getBreakReturnTime(),
                entity.getDepartureTime(),
                entity.getDate()
        );
    }
}
