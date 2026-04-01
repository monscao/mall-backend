package com.shopback.role.service;

import com.shopback.role.dto.RoleResponse;
import com.shopback.role.model.Role;
import com.shopback.role.repository.RoleRepository;
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
