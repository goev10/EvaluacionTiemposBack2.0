package com.web.back.controllers;

import com.web.back.filters.PermissionsFilter;
import com.web.back.model.dto.GrouperDto;
import com.web.back.model.requests.GrouperRequest;
import com.web.back.services.GroupersService;
import com.web.back.services.JwtService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/groupers")
@Tag(name = "Groupers Configuration")
public class GroupersController {
    private final JwtService jwtService;
    private final GroupersService groupersService;

    public GroupersController(JwtService jwtService, GroupersService groupersService) {
        this.jwtService = jwtService;
        this.groupersService = groupersService;
    }

    @GetMapping
    public ResponseEntity<List<GrouperDto>> getAll() {
        var permissions = jwtService.getCurrentUserPermissions();
        if (!PermissionsFilter.canRead(permissions)) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(groupersService.getAll());
    }

    @GetMapping("{id}")
    public ResponseEntity<GrouperDto> getById(@PathVariable int id) {
        var permissions = jwtService.getCurrentUserPermissions();
        if (!PermissionsFilter.canRead(permissions)) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(groupersService.getById(id));
    }

    @PostMapping
    public ResponseEntity<GrouperDto> add(@RequestBody GrouperRequest request) {
        var permissions = jwtService.getCurrentUserPermissions();
        if (!PermissionsFilter.canCreate(permissions)) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(groupersService.add(request));
    }

    @PutMapping("{id}")
    public ResponseEntity<GrouperDto> update(@PathVariable int id, @RequestBody GrouperRequest request) {
        var permissions = jwtService.getCurrentUserPermissions();
        if (!PermissionsFilter.canEdit(permissions)) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(groupersService.update(id, request));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        var permissions = jwtService.getCurrentUserPermissions();
        if (!PermissionsFilter.canDelete(permissions)) {
            return ResponseEntity.status(401).build();
        }

        groupersService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
