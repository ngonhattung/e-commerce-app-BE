package com.nhattung.authservice.service.role;

import com.nhattung.authservice.dto.RoleDto;
import com.nhattung.authservice.entity.Role;
import com.nhattung.authservice.repository.RoleRepository;
import com.nhattung.authservice.request.CreateRoleRequest;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class RoleService implements IRoleService{
    private final RoleRepository roleRepository;
    private final ModelMapper modelMapper;
    @Override
    public Role createRole(CreateRoleRequest request) {
        return Optional.of(request)
                .filter(r -> !roleRepository.existsByName(request.getName()))
                .map(req -> {
                    Role newRole = Role.builder()
                            .name(request.getName())
                            .build();
                    return roleRepository.save(newRole);
                }).orElseThrow(() -> new RuntimeException("Role not created!"));
    }

    @Override
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    @Override
    public RoleDto convertRoleToDto(Role role) {
        return modelMapper.map(role, RoleDto.class);
    }

    @Override
    public List<RoleDto> convertRolesToDtos(List<Role> roles) {
        return roles
                .stream()
                .map(this::convertRoleToDto).toList();
    }


    @Override
    public void deleteRole(Long roleId) {
        roleRepository.deleteById(roleId);
    }
}
