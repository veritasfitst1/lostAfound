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
    //发送给前端用 举报信息
    private Long id;  //举报记录主键
    private Long reporterId;  //举报人
    private String reporterNickname;
    private Long reportedUserId; //被举报人
    private String reportedUserNickname;
    private Long reportedItemId;  //被举报物品
    private String reportedItemTitle;
    private String reason;  //理由
    private Integer status; //举报信息处理状态 0 待审核 1通过 2驳回
    private String adminNote;   //管理员处理信息时的说明
    private LocalDateTime createdAt;   //举报时间
}
