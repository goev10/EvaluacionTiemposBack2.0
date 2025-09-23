package com.web.back.controllers;

import com.web.back.filters.PermissionsFilter;
import com.web.back.model.dto.EvaluationsDataDto;
import com.web.back.model.dto.GetEmployeesRequestDto;
import com.web.back.model.dto.RegistroTiemposDto;
import com.web.back.model.requests.GenerateXlsRequest;
import com.web.back.model.responses.CustomResponse;
import com.web.back.services.EmpleadoService;
import com.web.back.services.JwtService;
import com.web.back.utils.DateUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/empleado/")
@Tag(name = "Empleado")
public class EmpleadoController {
    private final EmpleadoService empleadoService;
    private final JwtService jwtService;

    public EmpleadoController(EmpleadoService empleadoService, JwtService jwtService) {
        this.empleadoService = empleadoService;
        this.jwtService = jwtService;
    }

    @PostMapping(value = "getAll")
    public ResponseEntity<CustomResponse<EvaluationsDataDto>> getEmployeesEvaluations(@RequestBody GetEmployeesRequestDto requestDto) {
        var permissions = jwtService.getCurrentUserPermissions();
        if (!PermissionsFilter.canRead(permissions)) {
            return ResponseEntity.status(401).build();
        }

        String username = jwtService.getCurrentUserName();

        return ResponseEntity.ok(empleadoService.getEmployeesByFilters(requestDto.beginDate(), requestDto.endDate(), requestDto.sociedad(), requestDto.areaNomina(), username, requestDto.extraEmployeesData()));
    }

    @PostMapping(value = "getAll/sync")
    public ResponseEntity<CustomResponse<EvaluationsDataDto>> getEmployeesEvaluationsAndSync(@RequestBody GetEmployeesRequestDto requestDto) {
        var permissions = jwtService.getCurrentUserPermissions();
        if (!PermissionsFilter.canCreate(permissions) && !PermissionsFilter.canEdit(permissions)) {
            return ResponseEntity.status(401).build();
        }

        String username = jwtService.getCurrentUserName();

        return ResponseEntity.ok(empleadoService.getEmployeesCleanSync(requestDto.beginDate(), requestDto.endDate(), requestDto.sociedad(), requestDto.areaNomina(), username, requestDto.extraEmployeesData()));
    }

    @GetMapping(value = "getTimesheetInfo")
    public ResponseEntity<CustomResponse<List<RegistroTiemposDto>>> getTimesheetInfo(String beginDate, String endDate) {
        var permissions = jwtService.getCurrentUserPermissions();
        if (!PermissionsFilter.canCreate(permissions) && !PermissionsFilter.canRead(permissions)) {
            return ResponseEntity.status(401).build();
        }

        String username = jwtService.getCurrentUserName();

        return ResponseEntity.ok(empleadoService.getRegistroTiempos(beginDate, endDate, username));
    }

    @PostMapping(value = "timesheets")
    public ResponseEntity<CustomResponse<Void>> sendTimeSheetChanges(@RequestBody List<RegistroTiemposDto> registroTiemposDtos) {
        var permissions = jwtService.getCurrentUserPermissions();
        if (!PermissionsFilter.canEdit(permissions)) {
            return ResponseEntity.status(401).build();
        }

        var response = empleadoService.sendTimeSheetChanges(registroTiemposDtos);

        if (response != null && response.getStatusCode().is2xxSuccessful()) {
            return ResponseEntity.ok(new CustomResponse<Void>().ok(null, "Cambios aplicados exitosamente!"));
        }

        return ResponseEntity.ok(new CustomResponse<Void>().internalError("Algo fallo al enviar los cambios. Contacta al administrador!"));
    }

    @PostMapping(value = "/incidencesReportToXls")
    public ResponseEntity<byte[]> logToExcel(@RequestBody GenerateXlsRequest request) {
        try {
            final byte[] data = empleadoService.getLogsXlsData(request);

            HttpHeaders header = new HttpHeaders();
            header.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8"));
            header.setContentLength(data.length);
            header.setContentDispositionFormData("filename", getFileName(request));

            return new ResponseEntity<>(data, header, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String getFileName(GenerateXlsRequest request) {
        return String.format("%s-%s-%s-%s.xlsx",
                DateUtil.clearSymbols(request.beginDate()),
                DateUtil.clearSymbols(request.endDate()),
                request.sociedad().replace(" ", ""),
                request.areaNomina().replace(" ", ""));
    }
}
