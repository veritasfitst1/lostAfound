package com.example.server.dto;

import lombok.Data;

@Data
public class UserUpdateRequest {
    //接受前端的user修改信息
    private String nickname;
    private String avatarUrl;
    private String phone;
}
