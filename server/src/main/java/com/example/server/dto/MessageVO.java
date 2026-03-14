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

    private Long id;
    private Long senderId;
    private String senderNickname;
    private String senderAvatarUrl;
    private Long receiverId;
    private String receiverNickname;
    private String content;
    private Integer msgType;
    private Integer isRead;
    private LocalDateTime createdAt;
}
