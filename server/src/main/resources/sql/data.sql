-- 校园失物招领系统 - 测试数据 (MySQL 8.0)

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 用户数据 (密码 admin123 的 BCrypt 哈希)
-- ----------------------------
INSERT INTO `user` (`openid`, `username`, `nickname`, `avatar_url`, `phone`, `password`, `role`, `status`) VALUES
(NULL, 'admin', '系统管理员', NULL, '13800138000', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'ADMIN', 0),
('wx_test_user_001', NULL, '小明', 'https://thirdwx.qlogo.cn/mmopen/placeholder/132', '13911112222', NULL, 'USER', 0),
('wx_test_user_002', NULL, '小红', 'https://thirdwx.qlogo.cn/mmopen/placeholder/132', '13922223333', NULL, 'USER', 0),
('wx_test_user_003', NULL, '小李', 'https://thirdwx.qlogo.cn/mmopen/placeholder/132', '13933334444', NULL, 'USER', 0);

-- ----------------------------
-- 物品分类
-- ----------------------------
INSERT INTO `item_category` (`name`, `icon`, `sort_order`) VALUES
('证件卡类', 'idcard', 1),
('电子产品', 'mobile', 2),
('钥匙', 'key', 3),
('钱包', 'wallet', 4),
('书本资料', 'book', 5),
('雨伞', 'umbrella', 6),
('其他', 'other', 99);

-- ----------------------------
-- 物品信息 (失物+招领)
-- ----------------------------
INSERT INTO `item` (`user_id`, `category_id`, `type`, `title`, `description`, `location`, `images`, `contact`, `status`, `event_time`) VALUES
(2, 1, 0, '学生证', '红色卡套，姓名张三', '图书馆三楼阅览室', '["/uploads/test1.jpg"]', '13911112222', 0, '2025-03-10 14:00:00'),
(3, 2, 1, '黑色蓝牙耳机', 'AirPods Pro，在食堂捡到', '第一食堂', '["/uploads/test2.jpg"]', '13922223333', 0, '2025-03-11 12:30:00'),
(2, 2, 0, 'iPhone 手机', '深空灰，屏幕有贴膜', '教学楼A栋教室', '["/uploads/test3.jpg"]', '13911112222', 0, '2025-03-12 09:00:00'),
(4, 4, 1, '棕色钱包', '内有身份证和若干现金', '操场看台', NULL, '13933334444', 0, '2025-03-13 17:00:00'),
(3, 5, 0, '高等数学教材', '第七版同济版，有笔记', '自习室105', '["/uploads/test4.jpg"]', '13922223333', 1, '2025-03-08 10:00:00');

-- ----------------------------
-- 私信
-- ----------------------------
INSERT INTO `message` (`sender_id`, `receiver_id`, `content`, `msg_type`, `is_read`) VALUES
(2, 3, '您好，请问耳机还在吗？', 0, 1),
(3, 2, '在的，你可以来一食堂门口取', 0, 1),
(2, 3, '好的，谢谢！', 0, 0),
(4, 2, '请问钱包里有银行卡吗？', 0, 0);

-- ----------------------------
-- 物品留言
-- ----------------------------
INSERT INTO `item_comment` (`item_id`, `user_id`, `content`) VALUES
(1, 3, '我好像在图书馆见过，可以问问前台'),
(2, 2, '是我的！请联系我谢谢'),
(4, 2, '钱包里有我的身份证，非常感谢！');

-- ----------------------------
-- 举报 (待审核)
-- ----------------------------
INSERT INTO `report` (`reporter_id`, `reported_item_id`, `reason`, `status`) VALUES
(2, 2, '该物品描述不实，可能是虚假信息', 0);

SET FOREIGN_KEY_CHECKS = 1;
