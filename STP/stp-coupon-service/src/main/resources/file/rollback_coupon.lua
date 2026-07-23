-- KEYS[1] : 总库存Key (coupon:activity:stock)
-- KEYS[2] : 用户限额Hash Key前缀 (coupon:activity:user:limited:)
-- ARGV[1] : 用户ID (userId)
-- ARGV[2] : 活动id (activityId)
-- ARGV[3] : 优惠券id (couponId)
local couponId = ARGV[3]
local activityId = ARGV[2]
redis.call('hincrby', KEYS[1], activityId, 1)        -- 总库存 +1
redis.call('hincrby', KEYS[2] .. ARGV[1], couponId, -1)  -- 用户计数 -1
return 1
