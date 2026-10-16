-- 集成测试最小建表脚本：优惠券服务（coupon / coupon_use_scope / user_coupon）
-- 表结构摘自 init.sql 中对应表（去除与测试无关的注释/编码噪声）

DROP TABLE IF EXISTS `coupon`;

CREATE TABLE `coupon`
(
    `id`          bigint       NOT NULL AUTO_INCREMENT COMMENT '优惠券ID',
    `name`        varchar(128) NOT NULL COMMENT '优惠券名称',
    `discount`    decimal(3, 2)         DEFAULT NULL COMMENT '折扣比例',
    `amount`      decimal(10, 2)        DEFAULT NULL COMMENT '优惠金额',
    `type`        int                   DEFAULT '0' COMMENT '优惠券类型: 0折扣, 1金额',
    `status`      int                   DEFAULT '1' COMMENT '状态: 1可用, 0禁用',
    `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `scope_type`  int          NOT NULL DEFAULT '1' COMMENT '1全场通用 2指定商品分类 3指定商品',
    `time_type`   tinyint               DEFAULT '2' COMMENT '有效时间类型 1固定时间段 2领取后生效(天) 3领取后生效(小时)',
    `valid_days`  int                   DEFAULT NULL COMMENT '领取后有效天数',
    `valid_hours` int                   DEFAULT NULL COMMENT '领取后有效小时数',
    `image`       varchar(500)          DEFAULT NULL COMMENT '优惠券图片',
    `description` varchar(400) NOT NULL DEFAULT '' COMMENT '描述',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

DROP TABLE IF EXISTS `coupon_use_scope`;

CREATE TABLE `coupon_use_scope`
(
    `id`          bigint    NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `coupon_id`   bigint    NOT NULL COMMENT '优惠券模板ID',
    `relation_id` bigint    NOT NULL COMMENT '关联的实体ID',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_coupon_id` (`coupon_id`),
    KEY `idx_relation_id` (`relation_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

DROP TABLE IF EXISTS `user_coupon`;

CREATE TABLE `user_coupon`
(
    `id`          bigint    NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`     bigint    NOT NULL COMMENT '持有的用户ID',
    `coupon_id`   bigint    NOT NULL COMMENT '关联的优惠券模板ID',
    `status`      int       NOT NULL DEFAULT '1' COMMENT '使用状态: 0未使用, 1已使用',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '获得时间',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `used_time`   timestamp NULL     DEFAULT NULL COMMENT '核销使用时间',
    `end_time`    timestamp NULL     DEFAULT NULL COMMENT '过期时间',
    `order_id`    bigint             DEFAULT NULL COMMENT '订单id',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_coupon_status` (`user_id`, `coupon_id`, `status`),
    KEY `idx_user_id` (`user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
