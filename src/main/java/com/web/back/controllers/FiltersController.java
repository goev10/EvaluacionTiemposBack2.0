package com.web.back.controllers;

import com.web.back.filters.PermissionsFilter;
import com.web.back.model.responses.CustomResponse;
import com.web.back.model.responses.evaluacion.EvaluacionApiResponse;
import com.web.back.services.FiltersService;
import com.web.back.services.JwtService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/filters/")
@Tag(name = "Filters")
public class FiltersController {
    private final JwtService jwtService;
    private final FiltersService filtersService;

    public FiltersController(JwtService jwtService, FiltersService filtersService) {
        this.jwtService = jwtService;
        this.filtersService = filtersService;
    }

    @GetMapping(value="getFilerData")
    public ResponseEntity<CustomResponse<EvaluacionApiResponse>> getFilterDataS()
    {
        var permissions = jwtService.getCurrentUserPermissions();
        if (!PermissionsFilter.canRead(permissions)) {
            return ResponseEntity.status(401).build();
        }

        String username = jwtService.getCurrentUserName();

        var filters = filtersService.getFilters(username).block();

        return ResponseEntity.ok(new CustomResponse<EvaluacionApiResponse>().ok(filters));
    }
}
