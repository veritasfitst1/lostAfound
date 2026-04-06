package com.example.server.dto;

import lombok.Data;

@Data
public class WxLoginRequest {
    /** 小程序 wx.login 返回的临时 code，后端换取 openid */
    private String code;
}
