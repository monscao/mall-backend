package com.malllite.user.controller;

import com.malllite.auth.annotation.RequireAuth;
import com.malllite.auth.annotation.RequireRole;
import com.malllite.user.dto.UserResponse;
import com.malllite.user.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequireAuth
@RequireRole("ADMIN")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserResponse> listUsers() {
        return userService.listUsers();
    }
}
