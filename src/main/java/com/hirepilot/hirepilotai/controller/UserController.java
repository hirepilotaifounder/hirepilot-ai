package com.hirepilot.hirepilotai.controller;

import com.hirepilot.hirepilotai.dto.request.LoginRequest;
import com.hirepilot.hirepilotai.dto.response.LoginResponse;
import com.hirepilot.hirepilotai.entity.User;
import com.hirepilot.hirepilotai.service.UserService;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import com.hirepilot.hirepilotai.dto.request.UserRegistrationRequest;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public User registerUser(@Valid @RequestBody UserRegistrationRequest request) {
        return userService.registerUser(request);
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return userService.login(request);
    }
}