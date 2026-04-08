package com.example.server.controller;

import com.example.server.dto.BindWxRequest;
import com.example.server.dto.CommonResponse;
import com.example.server.dto.UserUpdateRequest;
import com.example.server.dto.UserVO;
import com.example.server.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    //获取个人信息
    @GetMapping("/me")
    public CommonResponse<UserVO> getProfile(@RequestAttribute Long userId) {
        return CommonResponse.ok(userService.getProfile(userId));
    }
    //更新个人信息
    @PutMapping("/me")
    public CommonResponse<UserVO> updateProfile(@RequestAttribute Long userId, @RequestBody UserUpdateRequest req) {
        return CommonResponse.ok(userService.updateProfile(userId, req.getNickname(), req.getAvatarUrl(), req.getPhone(),
                req.getUsername(), req.getPassword()));
    }

    //绑定微信
    @PostMapping("/bind-wx")
    public CommonResponse<UserVO> bindWx(@RequestAttribute Long userId, @Valid @RequestBody BindWxRequest req) {
        return CommonResponse.ok(userService.bindWx(userId, req.getCode()));
    }
}
