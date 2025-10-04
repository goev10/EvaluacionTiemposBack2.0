package com.web.back.services;

import com.web.back.model.dto.FieldValue;
import com.web.back.model.dto.evaluation.TheoreticalScheduleDto;
import com.web.back.model.dto.evaluation.TheoreticalTimesheetDay;
import com.web.back.model.entities.*;
import com.web.back.model.enumerators.ScheduleStatus;
import com.web.back.repositories.*;
import com.web.back.utils.FieldValueDictionary;
import org.apache.commons.jexl3.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.util.function.Tuples;

import java.lang.reflect.Field;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.temporal.ChronoField;
import java.util.*;

@Service
@Transactional
public class EvaluationService {

    private final TimeRecordRepository timeRecordRepository;
    private final TimeRuleRepository timeRuleRepository;
    private final TimesheetTimeRuleRepository timesheetTimeRuleRepository;
    private final EmployeeTimesheetsRepository employeeTimesheetsRepository;
    private final FestiveDaysRepository festiveDaysRepository;

    public EvaluationService(TimeRecordRepository timeRecordRepository,
                             TimeRuleRepository timeRuleRepository,
                             TimesheetTimeRuleRepository timesheetTimeRuleRepository, EmployeeTimesheetsRepository employeeTimesheetsRepository, FestiveDaysRepository festiveDaysRepository) {
        this.timeRecordRepository = timeRecordRepository;
        this.timeRuleRepository = timeRuleRepository;
        this.timesheetTimeRuleRepository = timesheetTimeRuleRepository;
        this.employeeTimesheetsRepository = employeeTimesheetsRepository;
        this.festiveDaysRepository = festiveDaysRepository;
    }

    public void generateEvaluations(Instant beginDate, Instant endDate) {
        List<TimeRecord> timeRecords = timeRecordRepository.findAllByDateBetween(beginDate, endDate);

        var employeeIds = timeRecords.stream()
                .map(tr -> tr.getEmployee().getId())
                .distinct()
                .toList();

        List<EmployeeTimesheet> timeSheetEmployees = employeeTimesheetsRepository.findOverlappingTimesheetsByIds(employeeIds, beginDate, endDate);

        var timeSheetIds = timeSheetEmployees.stream()
                .map(tse -> tse.getTimesheet().getId())
                .distinct()
                .toList();

        List<TimesheetTimeRule> timesheetTimeRules = timesheetTimeRuleRepository.findAllByTimesheetIdIn(timeSheetIds);

        var timeRuleIds = timesheetTimeRules.stream()
                .map(tr -> tr.getTimeRule().getId())
                .distinct()
                .toList();

        List<TimeRule> timeRuleEntities = timeRuleRepository.findAllById(timeRuleIds);

        List<FestiveDay> festiveDays = festiveDaysRepository.findAll();

        var theoreticalSchedulesWithTimeRecords = generateTheoreticalSchedule(
                employeeIds, timeRecords, timeSheetEmployees, festiveDays, beginDate, endDate);

        executeLevelOneRules(theoreticalSchedulesWithTimeRecords, timesheetTimeRules, timeRuleEntities);

        // Future implementation for level 2 and 3 rules can be added here
    }

    private void executeLevelOneRules(List<TheoreticalScheduleDto> theoreticalSchedulesWithTimeRecords,
                                      List<TimesheetTimeRule> timesheetTimeRules,
                                      List<TimeRule> timeRuleEntities) {
        var levelOneRules = timeRuleEntities.stream()
                .filter(tr -> tr.getLevel() == 1)
                .sorted(Comparator.comparing(TimeRule::getSequence))
                .toList();

        for (var schedule : theoreticalSchedulesWithTimeRecords) {
            for (var dayAndRecord : schedule.scheduleAndTimeRecords()) {
                var day = dayAndRecord.getT1();
                var recordOpt = dayAndRecord.getT2();

                List<String> generalResults = new ArrayList<>();
                for (var rule : levelOneRules) {
                    var applicableRules = timesheetTimeRules.stream()
                            .filter(ttr ->
                                    ttr.getTimesheet().getId().equals(day.timeSheetId()) &&
                                            ttr.getTimeRule().getId().equals(rule.getId()))
                            .toList();

                    if (applicableRules.isEmpty()) continue;

                    boolean conditionsMet = evaluateConditions(day, recordOpt.orElse(null), rule.getRule());
                    if (conditionsMet &&
                            rule.getResultMeets() != null && !rule.getResultMeets().isEmpty()) {
                        generalResults.add(rule.getResultMeets());
                    }

                    if(rule.isExclusive()) break;
                }

                if(!generalResults.isEmpty()) {
                    day.withGeneralResult(String.join("|", generalResults));
                }
            }
        }
    }

    private boolean evaluateConditions(TheoreticalTimesheetDay day, TimeRecord record, String rule) {
        var fieldValues = getFieldValues(day, record);

        return evaluateRule(rule, fieldValues);
    }

    private static Map<String, Object> getFieldValues(TheoreticalTimesheetDay day, TimeRecord record) {
        Map<String, Object> values = new HashMap<>();
        for (FieldValue fv : FieldValueDictionary.getDictionary()) {
            Object source = fv.getSourceClass().equals(TimeRecord.class) ? record : day;
            try {
                Field field = fv.getSourceClass().getDeclaredField(fv.getFieldName());
                field.setAccessible(true);
                Object value = field.get(source);
                values.put(fv.getKey(), value);
            } catch (Exception e) {
                values.put(fv.getKey(), null);
            }
        }
        return values;
    }

    public static boolean evaluateRule(String rule, Map<String, Object> fieldValues) {
        JexlEngine jexl = new JexlBuilder().create();
        JexlExpression expression = jexl.createExpression(rule);
        JexlContext context = new MapContext(fieldValues);

        Object result = expression.evaluate(context);
        if (result instanceof Boolean) {
            return (Boolean) result;
        } else if (result instanceof Number) {
            return ((Number) result).doubleValue() != 0;
        }
        return false;
    }

    private List<TheoreticalScheduleDto> generateTheoreticalSchedule(List<UUID> employeeIds,
                                                                     List<TimeRecord> timeRecords,
                                                                     List<EmployeeTimesheet> timeSheetEmployees,
                                                                     List<FestiveDay> festiveDays,
                                                                     Instant beginDate,
                                                                     Instant endDate) {
        List<TheoreticalScheduleDto> result = new ArrayList<>();

        for (Instant date = beginDate; !date.isAfter(endDate); date = date.plus(Duration.ofDays(1))) {
            Instant currentDate = date;
            boolean isFestive = festiveDays.stream()
                    .anyMatch(fd -> fd.getDay().equals(currentDate.get(ChronoField.DAY_OF_MONTH))
                            && fd.getMonth().equals(currentDate.get(ChronoField.MONTH_OF_YEAR)));

            if (isFestive) {
                for (UUID employeeId : employeeIds) {
                    addScheduleDay(result, employeeId, date, null, ScheduleStatus.FERI, timeRecords, currentDate);
                }
                continue;
            }

            var timesheetsForDate = timeSheetEmployees.stream()
                    .filter(ets ->
                            !currentDate.isBefore(ets.getFromDate()) && !currentDate.isAfter(ets.getToDate()))
                    .toList();

            for (var employeeTimesheet : timesheetsForDate) {
                var employeeId = employeeTimesheet.getEmployee().getId();
                var timesheet = employeeTimesheet.getTimesheet();
                addScheduleDay(result, employeeId, date, timesheet, ScheduleStatus.NORM, timeRecords, currentDate);
            }

            var employeesWithoutTimesheets = employeeIds.stream()
                    .filter(eid -> timesheetsForDate.stream()
                            .noneMatch(ets -> ets.getEmployee().getId().equals(eid)))
                    .toList();

            for (UUID employeeId : employeesWithoutTimesheets) {
                addScheduleDay(result, employeeId, date, null, ScheduleStatus.DESC, timeRecords, currentDate);
            }
        }

        return result;
    }

    private void addScheduleDay(List<TheoreticalScheduleDto> result,
                                UUID employeeId,
                                Instant date,
                                Timesheet timesheet,
                                ScheduleStatus status,
                                List<TimeRecord> timeRecords,
                                Instant currentDate) {
        TheoreticalTimesheetDay theoreticalDay = getTheoreticalDay(date, timesheet, status);

        var employeeTimeRecord = timeRecords.stream()
                .filter(tr -> tr.getEmployee().getId().equals(employeeId) && tr.getDate().equals(currentDate))
                .findFirst();

        TheoreticalScheduleDto dto = result.stream()
                .filter(d -> d.employeeId().equals(employeeId.toString()))
                .findFirst()
                .orElseGet(() -> {
                    TheoreticalScheduleDto newDto = new TheoreticalScheduleDto(employeeId.toString(), new ArrayList<>());
                    result.add(newDto);
                    return newDto;
                });

        dto.scheduleAndTimeRecords().add(Tuples.of(theoreticalDay, employeeTimeRecord));
    }

    private static TheoreticalTimesheetDay getTheoreticalDay(Instant date, Timesheet timesheet, ScheduleStatus status) {
        TheoreticalTimesheetDay theoreticalDay;
        if (ScheduleStatus.NORM.equals(status) && timesheet != null) {
            theoreticalDay = new TheoreticalTimesheetDay(
                    date, timesheet.getId(), timesheet.getEntryTime(),
                    timesheet.getBreakDepartureTime(), timesheet.getBreakReturnTime(),
                    timesheet.getDepartureTime(), status.toString());
        } else {
            theoreticalDay = new TheoreticalTimesheetDay(
                    date, null, LocalTime.of(0,0), LocalTime.of(0,0),
                    LocalTime.of(0,0), LocalTime.of(0,0), status.toString());
        }
        return theoreticalDay;
    }
}
