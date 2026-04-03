package com.example.server.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "item_category")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemCategory {
    //主键，物品分类唯一id标识
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    //分类名称
    @Column(name = "name", nullable = false, length = 32)
    private String name;
    //分类图标名称 给前端用
    @Column(name = "icon", length = 64)
    private String icon;
    //排序权重 用于前端展示顺序
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
