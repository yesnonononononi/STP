-- 数据库初始化脚本 (包含唯一索引优化)

-- 1. 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `uname` VARCHAR(64) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '加密密码',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `status_code` INT DEFAULT 1 COMMENT '用户状态: 1激活, 0封禁',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`uname`),
    UNIQUE KEY `uk_phone` (`phone`) -- 手机号唯一索引，防止重复注册
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. 商品表
CREATE TABLE IF NOT EXISTS `commodity` (
    `id` BIGINT NOT NULL COMMENT '商品ID',
    `name` VARCHAR(128) NOT NULL COMMENT '商品名称',
    `price` DECIMAL(10,2) NOT NULL COMMENT '商品价格',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- 3. 优惠券表
CREATE TABLE IF NOT EXISTS `coupon` (
    `id` BIGINT NOT NULL COMMENT '优惠券ID',
    `name` VARCHAR(128) NOT NULL COMMENT '优惠券名称',
    `discount` DECIMAL(3,2) DEFAULT NULL COMMENT '折扣比例 (如 0.85)',
    `amount` DECIMAL(10,2) DEFAULT NULL COMMENT '优惠金额',
    `type` INT DEFAULT 0 COMMENT '优惠券类型: 0折扣, 1金额',
    `status` INT DEFAULT 1 COMMENT '状态: 1可用, 0禁用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券表';

-- 4. 支付订单表
CREATE TABLE IF NOT EXISTS `payment_order` (
    `id` BIGINT NOT NULL COMMENT '订单ID',
    `creator_id` BIGINT NOT NULL COMMENT '创建者用户ID',
    `price` DECIMAL(10,2) NOT NULL COMMENT '订单金额',
    `pay_type` INT DEFAULT NULL COMMENT '支付类型: 1支付宝, 2微信',
    `to_name` VARCHAR(128) DEFAULT NULL COMMENT '收款方名称',
    `sign` VARCHAR(512) DEFAULT NULL COMMENT '支付签名',
    `status` INT DEFAULT 0 COMMENT '订单状态: 0待支付, 1已支付, 2已取消',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付订单表';

-- 5. 购物车表
CREATE TABLE IF NOT EXISTS `shopping_cart` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `uname` VARCHAR(64) NOT NULL COMMENT '关联用户名',
    `commodity_id` BIGINT NOT NULL COMMENT '商品ID',
    `quantity` INT NOT NULL DEFAULT 1 COMMENT '购买数量',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_commodity` (`uname`, `commodity_id`) -- 用户商品联合唯一索引，防止购物车记录重复
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车表';
