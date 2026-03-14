package com.example.server.dto;

import lombok.Data;

@Data
public class UserUpdateRequest {

    private String nickname;
    private String avatarUrl;
    private String phone;
}
