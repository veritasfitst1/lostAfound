package com.example.server.controller;

import com.example.server.dto.CommonResponse;
import com.example.server.dto.UserUpdateRequest;
import com.example.server.dto.UserVO;
import com.example.server.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public CommonResponse<UserVO> getProfile(@RequestAttribute Long userId) {
        return CommonResponse.ok(userService.getProfile(userId));
    }

    @PutMapping("/me")
    public CommonResponse<UserVO> updateProfile(@RequestAttribute Long userId, @RequestBody UserUpdateRequest req) {
        return CommonResponse.ok(userService.updateProfile(userId, req.getNickname(), req.getAvatarUrl(), req.getPhone()));
    }
}
