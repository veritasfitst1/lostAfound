package com.example.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    //登录用 统一返回给前端的用户信息数据结构，包括jwt token和user信息
    private String token;
    private UserVO user;
}
