package com.web.back.controllers;

import com.web.back.filters.PermissionsFilter;
import com.web.back.model.dto.EmployeeTimeSheetDto;
import com.web.back.model.requests.EmployeeTimeSheetRequest;
import com.web.back.services.EmployeeTimeSheetService;
import com.web.back.services.JwtService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/employee-timesheet")
@Tag(name = "Employee Timesheet")
public class EmployeeTimesheetController {
    private final EmployeeTimeSheetService employeeTimeSheetService;
    private final JwtService jwtService;

    public EmployeeTimesheetController(EmployeeTimeSheetService employeeTimeSheetService, JwtService jwtService) {
        this.employeeTimeSheetService = employeeTimeSheetService;
        this.jwtService = jwtService;
    }

    @GetMapping
    public ResponseEntity<List<EmployeeTimeSheetDto>> getAll() {
        var permissions = jwtService.getCurrentUserPermissions();
        if (!PermissionsFilter.canRead(permissions)) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(employeeTimeSheetService.getAll());
    }

    @GetMapping("{id}")
    public ResponseEntity<EmployeeTimeSheetDto> getById(@PathVariable String id) {
        var permissions = jwtService.getCurrentUserPermissions();
        if (!PermissionsFilter.canRead(permissions)) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(employeeTimeSheetService.getById(id));
    }

    @GetMapping("employee/{id}")
    public ResponseEntity<List<EmployeeTimeSheetDto>> getAllByEmployeeId(@PathVariable String id) {
        var permissions = jwtService.getCurrentUserPermissions();
        if (!PermissionsFilter.canRead(permissions)) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(employeeTimeSheetService.getAllByEmployeeId(UUID.fromString(id)));
    }

    @GetMapping("employee/number/{employeeNumber}")
    public ResponseEntity<List<EmployeeTimeSheetDto>> getAllByEmployeeNumber(@PathVariable String employeeNumber) {
        var permissions = jwtService.getCurrentUserPermissions();
        if (!PermissionsFilter.canRead(permissions)) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(employeeTimeSheetService.getAllByEmployeeNumber(employeeNumber));
    }

    @PostMapping
    public ResponseEntity<List<EmployeeTimeSheetDto>> add(@RequestBody EmployeeTimeSheetRequest request) {
        var permissions = jwtService.getCurrentUserPermissions();
        if (!PermissionsFilter.canCreate(permissions)) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(employeeTimeSheetService.add(request));
    }

    @PutMapping("{id}")
    public ResponseEntity<List<EmployeeTimeSheetDto>> add(
            @PathVariable String id,
            @RequestBody EmployeeTimeSheetRequest request) {
        var permissions = jwtService.getCurrentUserPermissions();
        if (!PermissionsFilter.canEdit(permissions)) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(employeeTimeSheetService.update(id, request));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteRelation(@PathVariable String id) {
        var permissions = jwtService.getCurrentUserPermissions();
        if (!PermissionsFilter.canDelete(permissions)) {
            return ResponseEntity.status(401).build();
        }

        employeeTimeSheetService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
