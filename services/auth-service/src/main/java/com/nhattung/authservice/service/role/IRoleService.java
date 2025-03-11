package com.nhattung.authservice.service.role;



import com.nhattung.authservice.dto.RoleDto;
import com.nhattung.authservice.entity.Role;
import com.nhattung.authservice.request.CreateRoleRequest;

import java.util.List;

public interface IRoleService {
    Role createRole(CreateRoleRequest request);
    List<Role> getAllRoles();
    RoleDto convertRoleToDto(Role role);
    void deleteRole(Long roleId);
}
