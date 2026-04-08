package com.example.server.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReportCreateRequest {
    //接受前端来的举报信息
    private Long reportedUserId;   //被举报用户
    private Long reportedItemId;   //被举报物品
    @NotBlank(message = "举报理由不能为空")
    private String reason;
}
