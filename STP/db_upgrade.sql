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


