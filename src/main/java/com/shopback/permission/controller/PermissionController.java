package com.shopback.permission.controller;

import com.shopback.auth.annotation.RequireAuth;
import com.shopback.auth.annotation.RequireRole;
import com.shopback.permission.dto.PermissionResponse;
import com.shopback.permission.service.PermissionService;
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
