package com.example.server.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CommentCreateRequest {
    //前端传进来的comment
    @NotBlank(message = "留言内容不能为空")
    private String content;
}
