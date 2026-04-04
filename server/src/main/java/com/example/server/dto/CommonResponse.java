package com.example.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
//公共响应类 由响应码、响应信息、响应体组成 如authresponse
public class CommonResponse<T> {

    //响应码
    private int code;
    //提示信息
    private String message;

    private T data;

    public static <T> CommonResponse<T> ok(T data) {
        return CommonResponse.<T>builder()
                .code(200)
                .message("success")
                .data(data)
                .build();
    }

    public static <T> CommonResponse<T> fail(int code, String message) {
        return CommonResponse.<T>builder()
                .code(code)
                .message(message)
                .data(null)
                .build();
    }
}
