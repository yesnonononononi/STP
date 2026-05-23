-- 为 payment_order 表添加 creator_id 字段
-- 执行时间：2026-05-21

ALTER TABLE `payment_order` 
ADD COLUMN `creator_id` BIGINT NOT NULL DEFAULT 0 COMMENT '创建者用户ID' AFTER `id`;

-- 如果需要为已有订单设置 creator_id，可以执行以下更新（根据实际情况调整）
-- UPDATE `payment_order` SET `creator_id` = ? WHERE `id` = ?;
