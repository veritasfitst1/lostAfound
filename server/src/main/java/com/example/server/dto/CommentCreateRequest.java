package com.example.server.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CommentCreateRequest {

    @NotBlank(message = "留言内容不能为空")
    private String content;
}
