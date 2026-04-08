package com.example.server.controller;

import com.example.server.dto.*;
import com.example.server.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
//用户注册登录认证
//处理认证逻辑，接收前端信息，进行验证，返回响应信息CommonResponse
public class AuthController {
    //AuthService管理注册和登录逻辑
    private final AuthService authService;

    //普通用户注册，对应微信一键登录按钮
    @PostMapping("/wx-login")
    public CommonResponse<AuthResponse> wxLogin(@RequestBody WxLoginRequest req) {
        return CommonResponse.ok(authService.wxLogin(req.getCode()));
    }
    //微信端普通用户使用用户名密码登录
    @PostMapping("/login")
    public CommonResponse<AuthResponse> login(@Valid @RequestBody AdminLoginRequest req) {
        return CommonResponse.ok(authService.userLogin(req.getUsername(), req.getPassword()));
    }

    //微信注册用户
    @PostMapping("/register")
    public CommonResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        return CommonResponse.ok(authService.register(req.getUsername(), req.getPassword(), req.getNickname()));
    }

    //管理员登录
    @PostMapping("/admin-login")
    public CommonResponse<AuthResponse> adminLogin(@Valid @RequestBody AdminLoginRequest req) {
        return CommonResponse.ok(authService.adminLogin(req.getUsername(), req.getPassword()));
    }
}
