package com.nhattung.authservice.controller;

import com.nhattung.authservice.dto.RoleDto;
import com.nhattung.authservice.entity.Role;
import com.nhattung.authservice.exception.AlreadyExistsException;
import com.nhattung.authservice.request.CreateRoleRequest;
import com.nhattung.authservice.response.ApiResponse;
import com.nhattung.authservice.service.role.IRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.CONFLICT;

@RequiredArgsConstructor
@RestController
@RequestMapping("/roles")
public class RoleController {
    private final IRoleService roleService;

    @GetMapping("/all")
    public ResponseEntity<ApiResponse> getAllRoles() {
        return ResponseEntity.ok(new ApiResponse("Success", roleService.getAllRoles()));
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse> createRole(@RequestBody CreateRoleRequest request) {
        try {
            Role role = roleService.createRole(request);
            RoleDto roleDto = roleService.convertRoleToDto(role);
            return ResponseEntity.ok(new ApiResponse("Role created successfully", roleDto));
        } catch (AlreadyExistsException e) {
            return ResponseEntity.status(CONFLICT).body(new ApiResponse(e.getMessage(), null));
        }
    }
    @DeleteMapping("/{roleId}/delete")
    public ResponseEntity<ApiResponse> deleteRole(@PathVariable Long roleId) {
        roleService.deleteRole(roleId);
        return ResponseEntity.ok(new ApiResponse("Role deleted successfully", null));
    }
}
