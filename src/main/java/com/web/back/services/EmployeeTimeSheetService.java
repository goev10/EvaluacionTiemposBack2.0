package com.web.back.services;

import com.web.back.model.dto.EmployeeTimeSheetDto;
import com.web.back.model.entities.Employee;
import com.web.back.model.entities.EmployeeTimesheet;
import com.web.back.model.entities.Timesheet;
import com.web.back.model.requests.EmployeeTimeSheetRequest;
import com.web.back.repositories.EmployeeRepository;
import com.web.back.repositories.EmployeeTimesheetsRepository;
import com.web.back.repositories.TimesheetsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class EmployeeTimeSheetService {
    private final EmployeeTimesheetsRepository employeeTimesheetsRepository;
    private final EmployeeRepository employeeRepository;
    private final TimesheetsRepository timesheetsRepository;

    public EmployeeTimeSheetService(EmployeeTimesheetsRepository employeeTimesheetsRepository, EmployeeRepository employeeRepository, TimesheetsRepository timesheetsRepository) {
        this.employeeTimesheetsRepository = employeeTimesheetsRepository;
        this.employeeRepository = employeeRepository;
        this.timesheetsRepository = timesheetsRepository;
    }

    public List<EmployeeTimeSheetDto> getAll() {
        return employeeTimesheetsRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public EmployeeTimeSheetDto getById(String id) {
        return employeeTimesheetsRepository.findById(UUID.fromString(id))
                .map(this::mapToDto)
                .orElseThrow(() -> new RuntimeException("Not found"));
    }

    public List<EmployeeTimeSheetDto> getAllByEmployeeId(UUID employeeId) {
        return employeeTimesheetsRepository.findAllByEmployeeId(employeeId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<EmployeeTimeSheetDto> getAllByEmployeeNumber(String employeeNumber) {
        return employeeTimesheetsRepository.findAllByEmployeeNumEmployee(employeeNumber)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<EmployeeTimeSheetDto> add(EmployeeTimeSheetRequest request) {
        var overlaps = employeeTimesheetsRepository.findOverlappingTimesheets(
                request.employeeNumber(), request.fromDate(), request.toDate());

        if (!overlaps.isEmpty()) {
            throw new RuntimeException("Employee already has a timesheet assigned in the given date range.");
        }

        EmployeeTimesheet entity = mapRequestToEntity(request);
        employeeTimesheetsRepository.save(entity);
        return getAll();
    }

    public List<EmployeeTimeSheetDto> update(String id, EmployeeTimeSheetRequest request) {
        var overlaps = employeeTimesheetsRepository.findOverlappingTimesheets(
                request.employeeNumber(), request.fromDate(), request.toDate())
                .stream().filter(entity -> !entity.getId().equals(UUID.fromString(id)))
                .toList();

        if (!overlaps.isEmpty()) {
            throw new RuntimeException("Employee already has a timesheet assigned in the given date range.");
        }

        EmployeeTimesheet entity = employeeTimesheetsRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new RuntimeException("Not found"));
        updateEntityFromRequest(entity, request);
        employeeTimesheetsRepository.save(entity);
        return getAll();
    }

    public void deleteById(String id) {
        employeeTimesheetsRepository.deleteById(UUID.fromString(id));
    }

    private EmployeeTimeSheetDto mapToDto(EmployeeTimesheet entity) {
        return new EmployeeTimeSheetDto(
                entity.getId().toString(),
                entity.getEmployee().getId().toString(),
                entity.getEmployee().getNumEmployee(),
                entity.getTimesheet().getId().toString(),
                entity.getTimesheet().getTimesheetIdentifier(),
                entity.getFromDate(),
                entity.getToDate()
        );
    }

    private EmployeeTimesheet mapRequestToEntity(EmployeeTimeSheetRequest request) {
        Employee employee = employeeRepository.findByNumEmployee(request.employeeNumber())
                .orElseThrow(() -> new RuntimeException("Provided employee doesn't exist"));
        Timesheet timesheet = timesheetsRepository.findByTimesheetIdentifier(request.timeSheetIdentifier())
                .orElseThrow(() -> new RuntimeException("provided timesheet doesn't exist"));

        EmployeeTimesheet entity = new EmployeeTimesheet();
        entity.setEmployee(employee);
        entity.setTimesheet(timesheet);
        entity.setFromDate(request.fromDate());
        entity.setToDate(request.toDate());
        return entity;
    }

    private void updateEntityFromRequest(EmployeeTimesheet entity, EmployeeTimeSheetRequest request) {
        entity.setFromDate(request.fromDate());
        entity.setToDate(request.toDate());
    }
}
