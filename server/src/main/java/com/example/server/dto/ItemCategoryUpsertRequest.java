package com.example.server.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ItemCategoryUpsertRequest {
    //接收前端的信息，增加或删除itemcategory
    @NotBlank(message = "分类名称不能为空")
    @Size(max = 32, message = "分类名称最多32个字符")
    private String name;

    @Size(max = 64, message = "图标标识最多64个字符")
    private String icon;

    @NotNull(message = "排序不能为空")
    private Integer sortOrder;
}
