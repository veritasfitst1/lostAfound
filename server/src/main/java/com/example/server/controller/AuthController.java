package com.example.server.controller;

import com.example.server.dto.*;
import com.example.server.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/wx-login")
    public CommonResponse<AuthResponse> wxLogin(@RequestBody WxLoginRequest req) {
        return CommonResponse.ok(authService.wxLogin(req.getOpenid(), req.getNickname(), req.getAvatarUrl()));
    }

    @PostMapping("/admin-login")
    public CommonResponse<AuthResponse> adminLogin(@Valid @RequestBody AdminLoginRequest req) {
        return CommonResponse.ok(authService.adminLogin(req.getUsername(), req.getPassword()));
    }
}
