-- 1. 重命名会员套餐表
RENAME TABLE `member` TO `member_package`;

-- 2. 修改订单表 payment_order 结构
ALTER TABLE `payment_order` 
    CHANGE COLUMN `commodity_id` `package_id` BIGINT NOT NULL COMMENT '会员套餐商品ID',
    CHANGE COLUMN `price` `amount` DECIMAL(10,2) NOT NULL COMMENT '实际支付总金额',
    ADD COLUMN `unit_price` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '购买时的单价快照',
    ADD COLUMN `discount_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '优惠券优惠金额快照';

-- 3. 修改用户持有优惠券表 user_coupon 结构
ALTER TABLE `user_coupon`
    ADD COLUMN `order_id` BIGINT DEFAULT NULL COMMENT '关联核销的订单ID',
    ADD COLUMN `used_time` TIMESTAMP DEFAULT NULL COMMENT '核销使用时间';

-- 4. 移除会员套餐表的 stock 冗余字段
ALTER TABLE `member_package` DROP COLUMN `stock`;

-- 5. 用户-会员映射表 user_member 新增 package_type_id 字段
ALTER TABLE `user_member` ADD COLUMN `package_type_id` BIGINT DEFAULT NULL COMMENT '当前购买的会员套餐类型ID';

-- 6. 会员套餐表 member_package 新增 daily_rate 字段
ALTER TABLE `member_package` ADD COLUMN `daily_rate` DECIMAL(10,4) NOT NULL COMMENT '日折算单价';

-- 7. 会员套餐表 member_package 新增 priority 字段
ALTER TABLE `member_package` ADD COLUMN `priority` INT NOT NULL DEFAULT 1 COMMENT '会员优先级，越大等级越高';

-- 8. 用户-会员映射表 user_member 新增 expire_time 和 daily_rate 字段，以支持会员过期时间及折算价值持久化
ALTER TABLE `user_member` ADD COLUMN `expire_time` DATETIME NOT NULL DEFAULT NOW() COMMENT '会员过期时间';
ALTER TABLE `user_member` ADD COLUMN `daily_rate` DECIMAL(10,4) NOT NULL DEFAULT 0.0000 COMMENT '当前会员日折算单价';

-- 9. 新增用户统计表，并抽离用户表计数器字段以防高并发锁冲突
CREATE TABLE IF NOT EXISTS `user_stat` (
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `fans` BIGINT NOT NULL DEFAULT 0 COMMENT '粉丝数',
    `topic` BIGINT NOT NULL DEFAULT 0 COMMENT '话题/帖子数',
    `liked` BIGINT NOT NULL DEFAULT 0 COMMENT '获赞/喜欢的作品数',
    PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户统计表';

-- 迁移现有计数数据至统计表
INSERT INTO `user_stat` (`user_id`, `fans`, `topic`, `liked`)
SELECT `id`, `fans`, `topic`, `liked` FROM `user`
ON DUPLICATE KEY UPDATE 
  `fans` = VALUES(`fans`), 
  `topic` = VALUES(`topic`), 
  `liked` = VALUES(`liked`);

-- 移除原核心用户表中的计数冗余字段
ALTER TABLE `user` DROP COLUMN `fans`;
ALTER TABLE `user` DROP COLUMN `topic`;
ALTER TABLE `user` DROP COLUMN `liked`;

-- 10. 移除用户主键自增属性以适配应用端雪花算法ID生成
ALTER TABLE `user` MODIFY COLUMN `id` BIGINT NOT NULL COMMENT '用户ID';

-- 11. 补齐 posts 表缺失的 media_urls 字段
ALTER TABLE `posts` ADD COLUMN `media_urls` TEXT DEFAULT NULL COMMENT '媒体资源URL列表' AFTER `content`;

-- 12. 新增帖子点赞, 帖子收藏, 评论点赞关系表 并清理 posts 计数冗余字段
CREATE TABLE IF NOT EXISTS `post_like` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `post_id` BIGINT NOT NULL COMMENT '帖子ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
    UNIQUE KEY `uk_post_user` (`post_id`, `user_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子点赞关系表';

CREATE TABLE IF NOT EXISTS `post_collect` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `post_id` BIGINT NOT NULL COMMENT '帖子ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    UNIQUE KEY `uk_post_user` (`post_id`, `user_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子收藏关系表';

CREATE TABLE IF NOT EXISTS `comment_like` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `comment_id` BIGINT NOT NULL COMMENT '评论ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '评论点赞时间',
    UNIQUE KEY `uk_comment_user` (`comment_id`, `user_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论点赞关系表';

ALTER TABLE `posts` DROP COLUMN `like_count`;
ALTER TABLE `posts` DROP COLUMN `collect_count`;


