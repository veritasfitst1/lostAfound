package com.example.server.dto;

import lombok.Data;

@Data
public class WxLoginRequest {
    //前端登录接口请求体，微信用户登录用 接受前端信息
    private String openid;
    private String nickname;
    private String avatarUrl;
}
