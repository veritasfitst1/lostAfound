-- 校园失物招领系统 - 数据库建表语句 (MySQL 8.0)
-- 字符集 utf8mb4 支持完整 Unicode（含 emoji）

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 用户表
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `openid` VARCHAR(64) DEFAULT NULL COMMENT '微信 openid',
    `username` VARCHAR(64) DEFAULT NULL COMMENT '用户名(管理员登录用)',
    `nickname` VARCHAR(64) DEFAULT '微信用户',
    `avatar_url` VARCHAR(512) DEFAULT NULL COMMENT '头像 URL',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '联系方式',
    `password` VARCHAR(128) DEFAULT NULL COMMENT '密码(BCrypt，仅管理员使用)',
    `role` VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT 'USER/ADMIN',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0正常 1封禁',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_openid` (`openid`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_status` (`status`),
    KEY `idx_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ----------------------------
-- 物品分类表
-- ----------------------------
DROP TABLE IF EXISTS `item_category`;
CREATE TABLE `item_category` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(32) NOT NULL COMMENT '分类名称',
    `icon` VARCHAR(64) DEFAULT NULL COMMENT '图标名',
    `sort_order` INT NOT NULL DEFAULT 0,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='物品分类表';

-- ----------------------------
-- 物品信息表 (失物+招领共用)
-- ----------------------------
DROP TABLE IF EXISTS `item`;
CREATE TABLE `item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL COMMENT '发布用户 ID',
    `category_id` BIGINT NOT NULL COMMENT '分类 ID',
    `type` TINYINT NOT NULL COMMENT '0失物 1招领',
    `title` VARCHAR(128) NOT NULL COMMENT '物品名称/标题',
    `description` TEXT COMMENT '详细描述',
    `location` VARCHAR(128) DEFAULT NULL COMMENT '丢失/拾取地点',
    `images` JSON DEFAULT NULL COMMENT '图片 URL 数组',
    `contact` VARCHAR(128) DEFAULT NULL COMMENT '联系方式',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0寻找中 1已找回 2已撤销 3已过期',
    `event_time` DATETIME DEFAULT NULL COMMENT '丢失/拾取时间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_type` (`type`),
    KEY `idx_status` (`status`),
    KEY `idx_created_at` (`created_at`),
    KEY `idx_event_time` (`event_time`),
    FULLTEXT KEY `ft_title_desc` (`title`, `description`),
    CONSTRAINT `fk_item_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_item_category` FOREIGN KEY (`category_id`) REFERENCES `item_category` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='物品信息表';

-- ----------------------------
-- 用户私信表
-- ----------------------------
DROP TABLE IF EXISTS `message`;
CREATE TABLE `message` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `sender_id` BIGINT NOT NULL,
    `receiver_id` BIGINT NOT NULL,
    `content` TEXT NOT NULL COMMENT '消息内容',
    `msg_type` TINYINT NOT NULL DEFAULT 0 COMMENT '0文本 1图片',
    `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '0未读 1已读',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_sender_receiver` (`sender_id`, `receiver_id`),
    KEY `idx_receiver_id` (`receiver_id`),
    KEY `idx_created_at` (`created_at`),
    CONSTRAINT `fk_msg_sender` FOREIGN KEY (`sender_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_msg_receiver` FOREIGN KEY (`receiver_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户私信表';

-- ----------------------------
-- 物品留言表
-- ----------------------------
DROP TABLE IF EXISTS `item_comment`;
CREATE TABLE `item_comment` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `item_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `content` VARCHAR(512) NOT NULL COMMENT '留言内容',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_item_id` (`item_id`),
    KEY `idx_user_id` (`user_id`),
    CONSTRAINT `fk_comment_item` FOREIGN KEY (`item_id`) REFERENCES `item` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_comment_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='物品留言表';

-- ----------------------------
-- 举报表
-- ----------------------------
DROP TABLE IF EXISTS `report`;
CREATE TABLE `report` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `reporter_id` BIGINT NOT NULL COMMENT '举报人 ID',
    `reported_user_id` BIGINT DEFAULT NULL COMMENT '被举报用户 ID',
    `reported_item_id` BIGINT DEFAULT NULL COMMENT '被举报物品 ID',
    `reason` VARCHAR(256) NOT NULL COMMENT '举报理由',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0待审核 1通过 2驳回',
    `admin_note` VARCHAR(256) DEFAULT NULL COMMENT '管理员备注',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_reporter` (`reporter_id`),
    KEY `idx_reported_user` (`reported_user_id`),
    KEY `idx_reported_item` (`reported_item_id`),
    KEY `idx_status` (`status`),
    CONSTRAINT `fk_report_reporter` FOREIGN KEY (`reporter_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_report_user` FOREIGN KEY (`reported_user_id`) REFERENCES `user` (`id`) ON DELETE SET NULL,
    CONSTRAINT `fk_report_item` FOREIGN KEY (`reported_item_id`) REFERENCES `item` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='举报表';

SET FOREIGN_KEY_CHECKS = 1;
