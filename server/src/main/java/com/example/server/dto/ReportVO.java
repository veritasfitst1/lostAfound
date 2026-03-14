package com.example.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportVO {

    private Long id;
    private Long reporterId;
    private String reporterNickname;
    private Long reportedUserId;
    private String reportedUserNickname;
    private Long reportedItemId;
    private String reportedItemTitle;
    private String reason;
    private Integer status;
    private String adminNote;
    private LocalDateTime createdAt;
}
