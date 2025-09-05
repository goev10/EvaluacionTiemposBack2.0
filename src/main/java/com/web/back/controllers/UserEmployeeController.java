package com.web.back.controllers;

import com.web.back.filters.PermissionsFilter;
import com.web.back.model.dto.UserEmployeesDTO;
import com.web.back.services.JwtService;
import com.web.back.services.UserEmployeeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/user-employees/")
@RestController
@Tag(name = "User Employee")
public class UserEmployeeController {
    private final JwtService jwtService;
    private final UserEmployeeService userEmployeeService;

    public UserEmployeeController(JwtService jwtService, UserEmployeeService userEmployeeService) {
        this.jwtService = jwtService;
        this.userEmployeeService = userEmployeeService;
    }

    @GetMapping("{userId}")
    public ResponseEntity<UserEmployeesDTO> getAllByUserId(
            @RequestHeader("Authorization") String bearerToken,
            @PathVariable Integer userId) {
        if (!PermissionsFilter.canRead(jwtService.getPermissionsFromToken(bearerToken))) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(userEmployeeService.getEmployeeForUserIdEnriched(userId));
    }

    @PostMapping("{userId}/bulk")
    public ResponseEntity<UserEmployeesDTO> bulkInsert(
            @RequestHeader("Authorization") String bearerToken,
            @PathVariable Integer userId,
            @RequestBody List<String> employeeNumbers) {
        if (!PermissionsFilter.canCreate(jwtService.getPermissionsFromToken(bearerToken)) &&
                !PermissionsFilter.canEdit(jwtService.getPermissionsFromToken(bearerToken))) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(userEmployeeService.upsertUserEmployeeRelations(userId, employeeNumbers));
    }

    @DeleteMapping("{userId}/{employeeNumber}")
    public ResponseEntity<Void> deleteRelation(
            @RequestHeader("Authorization") String bearerToken,
            @PathVariable Integer userId,
            @PathVariable String employeeNumber) {
        if (!PermissionsFilter.canDelete(jwtService.getPermissionsFromToken(bearerToken))) {
            return ResponseEntity.status(401).build();
        }

        userEmployeeService.deleteUserEmployeeRelations(userId, employeeNumber);
        return ResponseEntity.noContent().build();
    }
}
