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
public class MessageVO {
    //发送给前端用， 存放双方的聊天信息  记录单条消息
    private Long id; //message id0
    private Long senderId;
    private String senderNickname;
    private String senderAvatarUrl; //发送方头像 URL
    private Long receiverId;
    private String receiverNickname;
    private String content;
    private Integer msgType; //消息类型：实体注释为 0 = 文本，1 = 图片（与 Message 表一致）
    private Integer isRead;  //是否已读：0 = 未读，1 = 已读
    private LocalDateTime createdAt; //消息发送/入库时间
}
