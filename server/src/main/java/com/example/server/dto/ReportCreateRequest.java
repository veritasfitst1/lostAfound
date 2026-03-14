package com.example.server.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReportCreateRequest {

    private Long reportedUserId;
    private Long reportedItemId;
    @NotBlank(message = "举报理由不能为空")
    private String reason;
}
