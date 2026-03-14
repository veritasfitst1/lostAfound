package com.example.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserVO {

    private Long id;
    private String openid;
    private String username;
    private String nickname;
    private String avatarUrl;
    private String phone;
    private String role;
    private Integer status;
}
