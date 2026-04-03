package com.example.server.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "item")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Item {
    //物品信息 主键
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    //发布人  多对一 一个人可以发布多条消息
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    //物品所属分类id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private ItemCategory category;
    //发布的信息类型 0 丢失 1 招领
    @Column(name = "type", nullable = false)
    private Integer type;
    //物品名称
    @Column(name = "title", nullable = false, length = 128)
    private String title;
    //描述 强制text
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    //丢失或拾取地点
    @Column(name = "location", length = 128)
    private String location;
    //图片信息
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "images", columnDefinition = "json")
    private List<String> images;
    //联系方式
    @Column(name = "contact", length = 128)
    private String contact;
    //0 寻找中 1 已找回 2 已撤销 3 已过期
    @Column(name = "status", nullable = false)
    private Integer status;
    //丢失或拾取时间
    @Column(name = "event_time")
    private LocalDateTime eventTime;

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
