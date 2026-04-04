package com.example.server.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReportCreateRequest {
    //接受前端来的举报信息
    private Long reportedUserId;
    private Long reportedItemId;
    @NotBlank(message = "举报理由不能为空")
    private String reason;
}
