package com.shopback.role.controller;

import com.shopback.auth.annotation.RequireAuth;
import com.shopback.auth.annotation.RequireRole;
import com.shopback.role.dto.RoleResponse;
import com.shopback.role.service.RoleService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequireAuth
@RequireRole("ADMIN")
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
