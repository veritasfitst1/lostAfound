package com.example.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemVO {

    private Long id;
    private Long userId;
    private String userNickname;
    private String userAvatarUrl;
    private Long categoryId;
    private String categoryName;
    private Integer type;
    private String title;
    private String description;
    private String location;
    private List<String> images;
    private String contact;
    private Integer status;
    private LocalDateTime eventTime;
    private LocalDateTime createdAt;
    private Integer commentCount;
}
