-- 1. 安全删除 coupon 表中的冗余字段 effective_time_calc
SET @dbname = DATABASE();
SET @tablename = 'coupon';
SET @columnname = 'effective_time_calc';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @columnname) > 0,
  'ALTER TABLE coupon DROP COLUMN effective_time_calc;',
  'SELECT 1;'
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2. 清空现有优惠券与活动数据
DELETE FROM user_coupon;
DELETE FROM coupon_activity;
DELETE FROM coupon;

-- 3. 重置自增主键
ALTER TABLE user_coupon AUTO_INCREMENT = 1;
ALTER TABLE coupon_activity AUTO_INCREMENT = 1;
ALTER TABLE coupon AUTO_INCREMENT = 1;

-- 4. 插入优惠券模板数据
-- time_type: 1-固定时间段, 2-领取后生效(按天), 3-领取后生效(按小时)
-- scope_type: 1-全场通用
-- type: 0-折扣, 1-金额
-- status: 1-正常
INSERT INTO coupon (id, name, discount, amount, type, status, scope_type, time_type, valid_days, valid_hours, image, description)
VALUES 
(1, '年中大促全场通用满减券', NULL, 50.00, 1, 1, 1, 1, NULL, NULL, NULL, '全场商品通用满减券，活动期间内有效'),
(2, '全场通用大额折扣券(7天)', 0.85, NULL, 0, 1, 1, 2, 7, NULL, NULL, '自领券之日起7天内全场通用，享受85折优惠'),
(3, '数码家电限时特惠神券', NULL, 100.00, 1, 1, 1, 3, NULL, 12, NULL, '自领券之日起12小时内有效，数码品类专享');

-- 5. 插入优惠券投放活动数据
-- status: 1-开启
-- 活动3：设置为 30 秒后开始，用于测试秒杀倒计时
-- 活动4：设置为已经开始，用于测试秒杀进行中
INSERT INTO coupon_activity (id, coupon_id, name, stock, activity_start_time, activity_end_time, status)
VALUES
(1, 1, '年中大促通用领券通道', 1000, NOW() - INTERVAL 1 DAY, NOW() + INTERVAL 30 DAY, 1),
(2, 2, '周末折扣大放送普通领券', 500, NOW() - INTERVAL 1 DAY, NOW() + INTERVAL 15 DAY, 1),
(3, 3, '数码神券秒杀(30秒后开始)', 5, NOW() + INTERVAL 30 SECOND, NOW() + INTERVAL 2 HOUR, 1),
(4, 2, '进行中大额折扣券秒杀(抢购中)', 10, NOW() - INTERVAL 10 MINUTE, NOW() + INTERVAL 1 HOUR, 1);

-- 6. 插入用户优惠券实例数据 (测试“我的优惠券”列表以及二级Tab本地过滤)
-- user_id = 1
-- 实例1: 新获得券 (仅领用了10分钟，有效期7天，领用占比极低，属于“新获得”)
INSERT INTO user_coupon (id, user_id, coupon_id, status, create_time, update_time, used_time, end_time)
VALUES (1, 1, 2, 1, NOW() - INTERVAL 10 MINUTE, NOW() - INTERVAL 10 MINUTE, NULL, NOW() + INTERVAL 7 DAY);

-- 实例2: 即将过期券 (已领用11小时50分钟，仅剩10分钟过期，总长12小时，剩余占比 < 20%，属于“即将过期”)
INSERT INTO user_coupon (id, user_id, coupon_id, status, create_time, update_time, used_time, end_time)
VALUES (2, 1, 3, 1, NOW() - INTERVAL 11 HOUR - INTERVAL 50 MINUTE, NOW() - INTERVAL 11 HOUR - INTERVAL 50 MINUTE, NULL, NOW() + INTERVAL 10 MINUTE);

-- 实例3: 普通券 (已领用15天，剩余15天，占比为50%，既不属于“新获得”也不属于“即将过期”，属于“全部”)
INSERT INTO user_coupon (id, user_id, coupon_id, status, create_time, update_time, used_time, end_time)
VALUES (3, 1, 1, 1, NOW() - INTERVAL 15 DAY, NOW() - INTERVAL 15 DAY, NULL, NOW() + INTERVAL 15 DAY);

-- 实例4: 已使用券 (不应该在我的优惠券可用列表中显示)
INSERT INTO user_coupon (id, user_id, coupon_id, status, create_time, update_time, used_time, end_time)
VALUES (4, 1, 1, 0, NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY, NOW() + INTERVAL 28 DAY);
