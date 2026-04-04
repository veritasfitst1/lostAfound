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
public class CommentVO {
    //返回给前端的信息集合
    private Long id;
    private Long itemId;
    private Long userId;
    private String userNickname;
    private String userAvatarUrl;
    private String content;
    private LocalDateTime createdAt;
}
