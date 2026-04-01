package com.shopback.permission.service;

import com.shopback.permission.dto.PermissionResponse;
import com.shopback.permission.model.Permission;
import com.shopback.permission.repository.PermissionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PermissionService {

    private final PermissionRepository permissionRepository;

    public PermissionService(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    public List<PermissionResponse> listPermissions() {
        return permissionRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private PermissionResponse toResponse(Permission permission) {
        return new PermissionResponse(
                permission.id(),
                permission.code(),
                permission.name(),
                permission.description()
        );
    }
}
