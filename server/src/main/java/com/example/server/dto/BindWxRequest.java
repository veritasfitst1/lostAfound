package com.example.server.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BindWxRequest {
    //绑定微信用
    @NotBlank(message = "code 不能为空")
    private String code;
}
