package com.web.back.controllers;

import com.web.back.filters.PermissionsFilter;
import com.web.back.model.requests.ImpersonateRequest;
import com.web.back.model.responses.CustomResponse;
import com.web.back.model.responses.ImpersonateResponse;
import com.web.back.services.ImpersonateService;
import com.web.back.services.JwtService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/impersonate/")
@Tag(name = "Impersonate")
public class ImpersonateController {
    private final ImpersonateService impersonateService;
    private final JwtService jwtService;

    public ImpersonateController(ImpersonateService impersonateService, JwtService jwtService) {
        this.impersonateService = impersonateService;
        this.jwtService = jwtService;
    }

    @PostMapping("/insert")
    public ResponseEntity<CustomResponse<ImpersonateResponse>> insert(@RequestBody ImpersonateRequest request) {
        var permissions = jwtService.getCurrentUserPermissions();
        if (!PermissionsFilter.canCreate(permissions)) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(impersonateService.saveImpersonation(request));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<CustomResponse<Boolean>> delete(@PathVariable Integer id) {
        var permissions = jwtService.getCurrentUserPermissions();
        if (!PermissionsFilter.canDelete(permissions)) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(impersonateService.deleteImpersonation(id));
    }

    @GetMapping("/getAll")
    public ResponseEntity<CustomResponse<List<ImpersonateResponse>>> getAll() {
        var permissions = jwtService.getCurrentUserPermissions();
        if (!PermissionsFilter.canRead(permissions)) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(impersonateService.getImpersonations());
    }

    @GetMapping("/getByUser")
    public ResponseEntity<CustomResponse<List<ImpersonateResponse>>> getByUser() {
        String username = jwtService.getCurrentUserName();
        return ResponseEntity.ok(impersonateService.getByUser(username));
    }
}
