package com.example.server.dto;

import lombok.Data;

@Data
public class UserUpdateRequest {
    //接受前端的user修改信息
    private String nickname;
    private String avatarUrl;
    private String phone;
    /** 修改用户名（非空时更新） */
    private String username;
    /** 新密码（非空时更新，将加密存储） */
    private String password;
}
