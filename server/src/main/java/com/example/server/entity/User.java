package com.example.server.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user")
@Getter
@Setter
@Builder //创建类更规范
@NoArgsConstructor
@AllArgsConstructor
public class User {
    //主键，用户唯一标识
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    //微信openid，普通用户登录用
    @Column(name = "openid", unique = true, length = 64)
    private String openid;
    //用户名，管理员登陆用
    @Column(name = "username", unique = true, length = 64)
    private String username;

    @Column(name = "nickname", length = 64)
    private String nickname;
    //头像url
    @Column(name = "avatar_url", length = 512)
    private String avatarUrl;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "password", length = 128)
    private String password;
    //用户身份，不允许null  varchar USER ADMIN
    @Column(name = "role", nullable = false, length = 20)
    private String role;
    //账号状态 0正常 1封禁
    @Column(name = "status", nullable = false)
    private Integer status;
    //注册时间，用于仪表盘统计
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    //最后更新时间，用于仪表盘统计
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    //在执行 INSERT（第一次保存到数据库）之前自动调用 onCreate()
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    //在执行 UPDATE（修改已有记录）之前自动调用 onUpdate()
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
