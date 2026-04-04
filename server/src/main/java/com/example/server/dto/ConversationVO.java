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
    //当前用户与某一个人的消息摘要，用于在微信小程序前端展示会话列表用
    private Long otherUserId;  //会话对方用户id
    private String otherUserNickname; //对方用户昵称
    private String otherUserAvatarUrl; //对方用户头像
    private String lastMessage;  //对方最后一条消息内容预览
    private LocalDateTime lastMessageTime;  //对方最后一条消息时间
    private int unreadCount;  //未读消息数目
}
