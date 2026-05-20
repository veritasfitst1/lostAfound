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
    /** 新密码（非空时更新） */
    private String password;
    /** 修改密码时必填，用于校验原密码 */
    private String oldPassword;
}
