package com.web.back.controllers;

import com.web.back.filters.PermissionsFilter;
import com.web.back.model.dto.UserGroupersDto;
import com.web.back.model.requests.UserGropersRequest;
import com.web.back.services.JwtService;
import com.web.back.services.UserGroupersService;
import com.web.back.services.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/groupers")
@Tag(name = "User Groupers")
public class UserGroupersController {
    private final JwtService jwtService;
    private final UserGroupersService userGroupersService;
    private final UserService userService;

    public UserGroupersController(JwtService jwtService, UserGroupersService userGroupersService, UserService userService) {
        this.jwtService = jwtService;
        this.userGroupersService = userGroupersService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserGroupersDto>> getAll() {
        var permissions = jwtService.getCurrentUserPermissions();
        if (!PermissionsFilter.canRead(permissions)) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(userGroupersService.getAll());
    }

    @GetMapping("{id}")
    public ResponseEntity<UserGroupersDto> getById(@PathVariable long id) {
        var permissions = jwtService.getCurrentUserPermissions();
        if (!PermissionsFilter.canRead(permissions)) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(userGroupersService.getById(id));
    }

    @GetMapping("user/{id}")
    public ResponseEntity<List<UserGroupersDto>> getAllByUserId(@PathVariable int id) {
        var permissions = jwtService.getCurrentUserPermissions();
        if (!PermissionsFilter.canRead(permissions)) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(userGroupersService.getAllByUserId(id));
    }

    @GetMapping("user/username/{userName}")
    public ResponseEntity<List<UserGroupersDto>> getAllByUserName(@PathVariable String userName) {
        var permissions = jwtService.getCurrentUserPermissions();
        if (!PermissionsFilter.canRead(permissions)) {
            return ResponseEntity.status(401).build();
        }

        var userGroupers = userService.getByUserName(userName)
                .map(user -> userGroupersService.getAllByUserId(user.getId()))
                .orElse(List.of());

        return ResponseEntity.ok(userGroupers);
    }

    @PostMapping
    public ResponseEntity<List<UserGroupersDto>> add(@RequestBody UserGropersRequest request) {
        var permissions = jwtService.getCurrentUserPermissions();
        if (!PermissionsFilter.canCreate(permissions)) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(userGroupersService.add(request));
    }

    @PutMapping("{id}")
    public ResponseEntity<List<UserGroupersDto>> update(
            @PathVariable Long id,
            @RequestBody UserGropersRequest request) {
        var permissions = jwtService.getCurrentUserPermissions();
        if (!PermissionsFilter.canEdit(permissions)) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(userGroupersService.update(id, request));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteRelation(@PathVariable Long id) {
        var permissions = jwtService.getCurrentUserPermissions();
        if (!PermissionsFilter.canDelete(permissions)) {
            return ResponseEntity.status(401).build();
        }

        userGroupersService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
