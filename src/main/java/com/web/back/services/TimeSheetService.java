package com.web.back.services;

import com.web.back.clients.ZWSHREvaluacioClient;
import com.web.back.mappers.DaysOfTheWeekMapper;
import com.web.back.model.dto.TimeSheetDto;
import com.web.back.model.entities.Timesheet;
import com.web.back.model.requests.RegistroHorariosRequest;
import com.web.back.model.requests.TimeSheetRequest;
import com.web.back.model.responses.RegistroHorariosResponse;
import com.web.back.repositories.TimesheetsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class TimeSheetService {

    private final ZWSHREvaluacioClient zwshrEvaluacioClient;
    private final TimesheetsRepository timesheetsRepository;

    public TimeSheetService(ZWSHREvaluacioClient zwshrEvaluacioClient, TimesheetsRepository timesheetsRepository) {
        this.zwshrEvaluacioClient = zwshrEvaluacioClient;
        this.timesheetsRepository = timesheetsRepository;
    }

    public List<RegistroHorariosResponse> registerTimeSheets(List<RegistroHorariosRequest> registroHorariosRequests) {

        return zwshrEvaluacioClient.postRegistroHorarios(registroHorariosRequests).block();
    }

    public List<TimeSheetDto> getAll() {
        return timesheetsRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    public TimeSheetDto getById(String id) {
        return timesheetsRepository.findById(UUID.fromString(id))
                .map(this::mapToDto)
                .orElse(null);
    }

    public TimeSheetDto getByTimesheetIdentifier(String id) {
        return timesheetsRepository.findByTimesheetIdentifier(id)
                .map(this::mapToDto)
                .orElse(null);
    }

    public List<TimeSheetDto> add(TimeSheetRequest request) {
        Timesheet entity = mapToEntity(request);
        Timesheet saved = timesheetsRepository.save(entity);
        return List.of(mapToDto(saved));
    }

    public List<TimeSheetDto> update(String id, TimeSheetRequest request) {
        return timesheetsRepository.findById(UUID.fromString(id))
                .map(existing -> {
                    mapToEntity(request, existing);

                    Timesheet updated = timesheetsRepository.save(existing);
                    return List.of(mapToDto(updated));
                })
                .orElse(List.of());
    }

    public void deleteById(String id) {
        timesheetsRepository.deleteById(UUID.fromString(id));
    }

    private Timesheet mapToEntity(TimeSheetRequest req) {
        Timesheet e = new Timesheet();
        mapToEntity(req, e);
        return e;
    }

    private void mapToEntity(TimeSheetRequest req, Timesheet e) {
        e.setTimesheetIdentifier(req.timeSheetIdentifier());
        e.setDescription(req.description());
        e.setDaysOfTheWeek(DaysOfTheWeekMapper.mapToBitmask(req.daysOfWeek()));
        e.setEntryTime(req.entryTime());
        e.setBreakDepartureTime(req.breakDepartureTime());
        e.setBreakReturnTime(req.breakReturnTime());
        e.setDepartureTime(req.departureTime());
    }

    private TimeSheetDto mapToDto(Timesheet entity) {
        return new TimeSheetDto(
                entity.getId().toString(),
                entity.getTimesheetIdentifier(),
                entity.getDescription(),
                DaysOfTheWeekMapper.mapDaysOfWeek(entity.getDaysOfTheWeek()),
                entity.getEntryTime(),
                entity.getBreakDepartureTime(),
                entity.getBreakReturnTime(),
                entity.getDepartureTime()
        );
    }
}
