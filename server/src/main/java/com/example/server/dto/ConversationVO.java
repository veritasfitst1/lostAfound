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
public class ConversationVO {

    private Long otherUserId;
    private String otherUserNickname;
    private String otherUserAvatarUrl;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private int unreadCount;
}
