package com.example.server.exception;

public class BusinessException extends RuntimeException {
    //异常码，不可修改
    private final int code;

    public BusinessException(String message) {
        super(message);
        this.code = 400;
    }
    //构造函数 设置异常码
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
