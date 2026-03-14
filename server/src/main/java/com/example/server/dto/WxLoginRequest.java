package com.example.server.dto;

import lombok.Data;

@Data
public class WxLoginRequest {

    private String openid;
    private String nickname;
    private String avatarUrl;
}
