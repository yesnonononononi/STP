-- KEYS[1] : 总库存Key (stock:couponId)
-- KEYS[2] : 用户限额Hash Key (user_limit:couponId)
-- ARGV[1] : 用户ID (userId)
local couponId = ARGV[3]
local activityId = ARGV[2]
redis.call('hincrby', KEYS[1],activityId,1)                 -- 总库存 +1
redis.call('hincrby', KEYS[2] + ARGV[1] + ':', couponId, -1) -- 用户计数 -1
return 1