package com.web.back.controllers;

import com.web.back.filters.PermissionsFilter;
import com.web.back.model.dto.TimeSheetDto;
import com.web.back.model.enumerators.PermissionsEnum;
import com.web.back.model.requests.RegistroHorariosRequest;
import com.web.back.model.requests.TimeSheetRequest;
import com.web.back.model.responses.CustomResponse;
import com.web.back.model.responses.RegistroHorariosResponse;
import com.web.back.services.JwtService;
import com.web.back.services.TimeSheetService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/timesheet")
@RestController
@Tag(name = "Timesheet")
public class TimeSheetController {
    private final TimeSheetService timeSheetService;
    private final JwtService jwtService;

    public TimeSheetController(TimeSheetService timeSheetService, JwtService jwtService) {
        this.timeSheetService = timeSheetService;
        this.jwtService = jwtService;
    }

    @PostMapping(value = "register/list")
    public ResponseEntity<CustomResponse<List<RegistroHorariosResponse>>> register(@RequestBody List<RegistroHorariosRequest> registroHorariosRequests) {
        var permissions = jwtService.getCurrentUserPermissions();
        if (!PermissionsFilter.hasPermission(permissions, PermissionsEnum.REGISTER_TIMESHEETS)) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(new CustomResponse<List<RegistroHorariosResponse>>().ok(
                timeSheetService.registerTimeSheets(registroHorariosRequests)
        ));
    }

    @GetMapping
    public ResponseEntity<List<TimeSheetDto>> getAll() {
        var permissions = jwtService.getCurrentUserPermissions();
        if (!PermissionsFilter.canRead(permissions)) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(timeSheetService.getAll());
    }

    @GetMapping("{id}")
    public ResponseEntity<TimeSheetDto> getById(@PathVariable String id) {
        var permissions = jwtService.getCurrentUserPermissions();
        if (!PermissionsFilter.canRead(permissions)) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(timeSheetService.getById(id));
    }

    @GetMapping("identifier/{id}")
    public ResponseEntity<TimeSheetDto> getByTimeSheetId(@PathVariable String id) {
        var permissions = jwtService.getCurrentUserPermissions();
        if (!PermissionsFilter.canRead(permissions)) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(timeSheetService.getByTimesheetIdentifier(id));
    }

    @PostMapping
    public ResponseEntity<List<TimeSheetDto>> add(@RequestBody TimeSheetRequest request) {
        var permissions = jwtService.getCurrentUserPermissions();
        if (!PermissionsFilter.canCreate(permissions)) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(timeSheetService.add(request));
    }

    @PutMapping("{id}")
    public ResponseEntity<List<TimeSheetDto>> add(
            @PathVariable String id,
            @RequestBody TimeSheetRequest request) {
        var permissions = jwtService.getCurrentUserPermissions();
        if (!PermissionsFilter.canEdit(permissions)) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(timeSheetService.update(id, request));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteRelation(@PathVariable String id) {
        var permissions = jwtService.getCurrentUserPermissions();
        if (!PermissionsFilter.canDelete(permissions)) {
            return ResponseEntity.status(401).build();
        }

        timeSheetService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
