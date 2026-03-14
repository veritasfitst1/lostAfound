package com.example.server.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ItemCreateRequest {

    @NotNull(message = "分类不能为空")
    private Long categoryId;

    @NotNull(message = "类型不能为空")
    private Integer type;

    @NotBlank(message = "标题不能为空")
    private String title;

    private String description;
    private String location;
    private List<String> images;
    private String contact;
    private LocalDateTime eventTime;
}
