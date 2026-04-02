package com.malllite.permission.controller;

import com.malllite.auth.annotation.RequireAuth;
import com.malllite.auth.annotation.RequireRole;
import com.malllite.permission.dto.PermissionResponse;
import com.malllite.permission.service.PermissionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/permissions")
@RequireAuth
@RequireRole("ADMIN")
public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @GetMapping
    public List<PermissionResponse> listPermissions() {
        return permissionService.listPermissions();
    }
}
