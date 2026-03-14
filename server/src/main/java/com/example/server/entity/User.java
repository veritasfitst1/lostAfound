package com.example.server.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "openid", unique = true, length = 64)
    private String openid;

    @Column(name = "username", unique = true, length = 64)
    private String username;

    @Column(name = "nickname", length = 64)
    private String nickname;

    @Column(name = "avatar_url", length = 512)
    private String avatarUrl;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "password", length = 128)
    private String password;

    @Column(name = "role", nullable = false, length = 20)
    private String role;

    @Column(name = "status", nullable = false)
    private Integer status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
