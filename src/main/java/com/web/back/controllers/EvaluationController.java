package com.web.back.controllers;

import com.web.back.filters.PermissionsFilter;
import com.web.back.model.dto.EvaluationDto;
import com.web.back.model.requests.EvaluationRequest;
import com.web.back.model.responses.CustomResponse;
import com.web.back.services.EvaluationService;
import com.web.back.services.JwtService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;

@RequestMapping("/evaluation/")
@RestController
@Tag(name = "Evaluations")
public class EvaluationController {
    private final JwtService jwtService;
    private final EvaluationService evaluationService;

    public EvaluationController(JwtService jwtService, EvaluationService evaluationService) {
        this.jwtService = jwtService;
        this.evaluationService = evaluationService;
    }

    @PutMapping(value="evaluations")
    public ResponseEntity<CustomResponse<List<EvaluationDto>>> updateRegistros(@RequestBody EvaluationRequest request)
    {
        request.setUserName(jwtService.getCurrentUserName());

        return ResponseEntity.ok(evaluationService.updateEvaluations(request));
    }

    @PostMapping(value="evaluations")
    public ResponseEntity<CustomResponse<Void>> sendApprovedEvaluationsToSap(@RequestBody EvaluationRequest request)
    {
        var permissions = jwtService.getCurrentUserPermissions();
        if (!PermissionsFilter.canEdit(permissions)) {
            return ResponseEntity.status(401).build();
        }

        try {
            evaluationService.sendApprovedEvaluationsToSap(request.getBeginDate(), request.getEndDate(), request.getSociedad(), request.getAreaNomina());

            return ResponseEntity.ok(new CustomResponse<Void>().ok(null, "Evaluaciones enviadas exitosamente!"));
        }catch (Exception e) {
            String errorMessage;

            if (e instanceof HttpClientErrorException) {
                errorMessage = "Error al enviar las evaluaciones a SAP. Contacta al administrador!";
            } else {
                errorMessage = e.getMessage();
            }
            return ResponseEntity.ok(new CustomResponse<Void>().internalError(errorMessage));
        }
    }

    @GetMapping(value = "evaluations")
    public ResponseEntity<CustomResponse<List<EvaluationDto>>> getAllEmployeeEvaluations() {
        var permissions = jwtService.getCurrentUserPermissions();
        if (!PermissionsFilter.isSuperUser(permissions)) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(new CustomResponse<List<EvaluationDto>>().ok(evaluationService.getAllEvaluations()));
    }

    @GetMapping(value = "evaluations/filtered")
    public ResponseEntity<CustomResponse<List<EvaluationDto>>> getAllEmployeeEvaluationsByFilters(String beginDate, String endDate, String sociedad, String areaNomina) {
        var permissions = jwtService.getCurrentUserPermissions();
        if (!PermissionsFilter.isSuperUser(permissions)) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(new CustomResponse<List<EvaluationDto>>().ok(evaluationService.getAllEvaluationsByFilters(beginDate, endDate, sociedad, areaNomina)));
    }

    @DeleteMapping(value = "evaluations")
    public ResponseEntity<CustomResponse<Void>> deleteEvaluations(@RequestBody List<Integer> evaluationsToRemove) {
        var permissions = jwtService.getCurrentUserPermissions();
        if (!PermissionsFilter.isSuperUser(permissions)) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(new CustomResponse<Void>().ok(evaluationService.deleteEvaluations(evaluationsToRemove)));
    }
}
