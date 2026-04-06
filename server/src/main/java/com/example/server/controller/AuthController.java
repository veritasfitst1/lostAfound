package com.example.server.controller;

import com.example.server.dto.*;
import com.example.server.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
//处理认证逻辑，接收前端信息，进行验证，返回响应信息CommonResponse
public class AuthController {
    //authservice用来处理认证任务
    private final AuthService authService;

    @PostMapping("/wx-login")
    public CommonResponse<AuthResponse> wxLogin(@RequestBody WxLoginRequest req) {
        return CommonResponse.ok(authService.wxLogin(req.getCode()));
    }

    @PostMapping("/login")
    public CommonResponse<AuthResponse> login(@Valid @RequestBody AdminLoginRequest req) {
        return CommonResponse.ok(authService.userLogin(req.getUsername(), req.getPassword()));
    }

    @PostMapping("/register")
    public CommonResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        return CommonResponse.ok(authService.register(req.getUsername(), req.getPassword(), req.getNickname()));
    }

    @PostMapping("/admin-login")
    public CommonResponse<AuthResponse> adminLogin(@Valid @RequestBody AdminLoginRequest req) {
        return CommonResponse.ok(authService.adminLogin(req.getUsername(), req.getPassword()));
    }
}
