package com.shopback.auth.controller;

import com.shopback.auth.annotation.RequireAuth;
import com.shopback.auth.context.AuthContext;
import com.shopback.auth.dto.AuthUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthContextController {

    @GetMapping("/me")
    @RequireAuth
    public AuthUser currentUser() {
        return AuthContext.getCurrentUser();
    }
}
