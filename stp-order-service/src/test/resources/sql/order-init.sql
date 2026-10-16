-- 集成测试最小建表脚本：订单服务（payment_order）
-- 表结构摘自 init.sql 中 payment_order（去除与测试无关的注释/编码噪声）

DROP TABLE IF EXISTS `payment_order`;

CREATE TABLE `payment_order`
(
    `id`              bigint         NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `amount`          decimal(10, 2) NOT NULL COMMENT '实际支付总金额',
    `pay_type`        int                     DEFAULT NULL COMMENT '支付类型: 1支付宝 2微信',
    `creator_id`      bigint         NOT NULL COMMENT '下单人id',
    `status`          int                     DEFAULT '0' COMMENT '订单状态: 0待支付 1已支付 2已完成 3已取消',
    `create_time`     datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `package_id`      bigint         NOT NULL COMMENT '会员套餐商品ID',
    `quantity`        int                     DEFAULT NULL COMMENT '购买数量',
    `coupon_id`       bigint                  DEFAULT NULL COMMENT '使用的优惠券ID',
    `unit_price`      decimal(10, 2) NOT NULL DEFAULT '0.00' COMMENT '购买时的单价快照',
    `discount_amount` decimal(10, 2) NOT NULL DEFAULT '0.00' COMMENT '优惠金额快照',
    `pay_time`        datetime                DEFAULT NULL COMMENT '支付完成时间',
    `timeout_time`    datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '超时时间',
    PRIMARY KEY (`id`),
    KEY `idx_payment_order_status_timeout_time_id` (`status`, `timeout_time`, `id`),
    KEY `idx_payment_order_create_time` (`create_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
