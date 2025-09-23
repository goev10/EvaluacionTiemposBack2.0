package com.web.back.controllers;

import com.web.back.filters.PermissionsFilter;
import com.web.back.model.dto.FestiveDayDto;
import com.web.back.model.requests.FestiveDayRequest;
import com.web.back.services.FestiveDaysService;
import com.web.back.services.JwtService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/festive-days")
@Tag(name = "MX Festive Days")
public class FestiveDaysController {
    private final JwtService jwtService;
    private final FestiveDaysService festiveDaysService;

    public FestiveDaysController(JwtService jwtService, FestiveDaysService festiveDaysService) {
        this.jwtService = jwtService;
        this.festiveDaysService = festiveDaysService;
    }

    @GetMapping
    public ResponseEntity<List<FestiveDayDto>> getAll() {
        var permissions = jwtService.getCurrentUserPermissions();
        if (!PermissionsFilter.canRead(permissions)) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(festiveDaysService.getAll());
    }

    @GetMapping("{id}")
    public ResponseEntity<FestiveDayDto> getById(@PathVariable long id) {
        var permissions = jwtService.getCurrentUserPermissions();
        if (!PermissionsFilter.canRead(permissions)) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(festiveDaysService.getById(id));
    }

    @PostMapping
    public ResponseEntity<FestiveDayDto> add(@RequestBody FestiveDayRequest request) {
        var permissions = jwtService.getCurrentUserPermissions();
        if (!PermissionsFilter.canCreate(permissions)) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(festiveDaysService.add(request));
    }

    @PutMapping("{id}")
    public ResponseEntity<FestiveDayDto> update(@PathVariable long id, @RequestBody FestiveDayRequest request) {
        var permissions = jwtService.getCurrentUserPermissions();
        if (!PermissionsFilter.canEdit(permissions)) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(festiveDaysService.update(id, request));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        var permissions = jwtService.getCurrentUserPermissions();
        if (!PermissionsFilter.canDelete(permissions)) {
            return ResponseEntity.status(401).build();
        }

        festiveDaysService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
