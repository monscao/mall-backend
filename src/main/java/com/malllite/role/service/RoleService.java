package com.malllite.role.service;

import com.malllite.role.dto.RoleResponse;
import com.malllite.role.model.Role;
import com.malllite.role.repository.RoleRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class RoleService {

    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public List<RoleResponse> listRoles() {
        List<Role> roles = roleRepository.findAll();
        Map<Long, List<String>> permissionCodesByRoleId = roleRepository.findPermissionCodesByRoleIds(
                roles.stream().map(Role::id).toList()
        );

        return roles.stream()
                .map(role -> toResponse(role, permissionCodesByRoleId.getOrDefault(role.id(), List.of())))
                .toList();
    }

    private RoleResponse toResponse(Role role, List<String> permissionCodes) {
        return new RoleResponse(
                role.id(),
                role.code(),
                role.name(),
                role.description(),
                permissionCodes
        );
    }
}
