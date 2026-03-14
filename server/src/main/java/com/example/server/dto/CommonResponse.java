package com.example.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommonResponse<T> {

    private int code;
    private String message;
    private T data;

    public static <T> CommonResponse<T> ok(T data) {
        return CommonResponse.<T>builder().code(200).message("success").data(data).build();
    }

    public static <T> CommonResponse<T> fail(int code, String message) {
        return CommonResponse.<T>builder().code(code).message(message).data(null).build();
    }
}
