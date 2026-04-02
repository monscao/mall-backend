package com.malllite.auth.controller;

import com.malllite.auth.annotation.RequireAuth;
import com.malllite.auth.context.AuthContext;
import com.malllite.auth.dto.AuthUser;
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
