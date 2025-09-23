package com.web.back.controllers;

import com.web.back.filters.PermissionsFilter;
import com.web.back.mappers.ProfileDtoMapper;
import com.web.back.model.dto.ProfileDto;
import com.web.back.model.requests.ProfileRequest;
import com.web.back.model.responses.CustomResponse;
import com.web.back.services.JwtService;
import com.web.back.services.ProfileService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/perfil/")
@RestController
@Tag(name = "Profile")
public class ProfileController {
    private final JwtService jwtService;
    private final ProfileService profileService;

    public ProfileController(final JwtService jwtService, final ProfileService profileService) {
        this.jwtService = jwtService;
        this.profileService = profileService;
    }

    @GetMapping(value = "getAll")
    public ResponseEntity<CustomResponse<List<ProfileDto>>> getAll() {
        var permissions = jwtService.getCurrentUserPermissions();
        if (!PermissionsFilter.canRead(permissions)) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(new CustomResponse<List<ProfileDto>>().ok(profileService.getALl().stream()
                .map(ProfileDtoMapper::mapFrom).toList()));
    }

    @PostMapping(value = "register")
    public ResponseEntity<CustomResponse<ProfileDto>> register(@RequestBody ProfileRequest request) {
        var permissions = jwtService.getCurrentUserPermissions();
        if (!PermissionsFilter.canCreate(permissions)) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(new CustomResponse<ProfileDto>().ok(
                ProfileDtoMapper.mapFrom(profileService.save(request))
        ));
    }

    @PutMapping(value = "update/{id}")
    public ResponseEntity<CustomResponse<ProfileDto>> update(@RequestBody ProfileRequest request, @PathVariable Integer id) {
        var permissions = jwtService.getCurrentUserPermissions();
        if (!PermissionsFilter.canEdit(permissions)) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(new CustomResponse<ProfileDto>().ok(
                ProfileDtoMapper.mapFrom(profileService.update(id, request))
        ));
    }

    @DeleteMapping(value = "delete/{id}")
    public ResponseEntity<CustomResponse<Void>> delete(@PathVariable Integer id) {
        var permissions = jwtService.getCurrentUserPermissions();
        if (!PermissionsFilter.canDelete(permissions)) {
            return ResponseEntity.status(401).build();
        }

        profileService.delete(id);

        return ResponseEntity.ok(new CustomResponse<Void>().ok(null));
    }
}
