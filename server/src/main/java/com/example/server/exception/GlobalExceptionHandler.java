package com.example.server.exception;

import com.example.server.dto.CommonResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<CommonResponse<?>> handleBusiness(BusinessException e) {
        return ResponseEntity.status(e.getCode() >= 400 ? e.getCode() : 400)
                .body(CommonResponse.fail(e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<CommonResponse<?>> handleAccessDenied() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(CommonResponse.fail(403, "无权限访问"));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<CommonResponse<?>> handleValidation(Exception e) {
        String msg = "参数校验失败";
        if (e instanceof MethodArgumentNotValidException ex) {
            var err = ex.getBindingResult().getFieldError();
            if (err != null) msg = err.getDefaultMessage();
        } else if (e instanceof BindException ex) {
            var err = ex.getBindingResult().getFieldError();
            if (err != null) msg = err.getDefaultMessage();
        }
        return ResponseEntity.badRequest().body(CommonResponse.fail(400, msg));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonResponse<?>> handleOther(Exception e) {
        log.error("Unhandled exception", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CommonResponse.fail(500, "服务器内部错误"));
    }
}
