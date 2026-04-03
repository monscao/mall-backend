package com.malllite.role.controller;

import com.malllite.auth.annotation.RequireAuth;
import com.malllite.auth.PermissionCodes;
import com.malllite.auth.annotation.RequirePermission;
import com.malllite.role.dto.RoleResponse;
import com.malllite.role.service.RoleService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequireAuth
@RequirePermission(PermissionCodes.ROLE_READ)
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    public List<RoleResponse> listRoles() {
        return roleService.listRoles();
    }
}
