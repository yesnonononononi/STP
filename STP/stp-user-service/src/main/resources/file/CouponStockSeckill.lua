
-- KEYS[1] : 总库存Hash Key (coupon:activity:stock)  -> field=activityId, value=stock
-- KEYS[2] : 用户限额Hash Key (user_limit:userId)    -> field=couponId, value=count
-- ARGV[1] : 个人领取上限 (limitCount)
-- ARGV[2] : 活动id (activityId)
-- ARGV[3] : 优惠券id (couponId)

local stock = redis.call('hget', KEYS[1],ARGV[2]);
local limitCount = ARGV[1];


if (tonumber(stock) == nil or( tonumber(stock) and tonumber(stock) <= 0)) then
    return -1
end

-- 检查用户可领取上限
local current = redis.call('hget', KEYS[2],ARGV[3]);
local currentNum = tonumber(current) or 0   -- 关键：nil 转为 0
if currentNum >= tonumber(limitCount) then
    return -2   -- 已达个人上限
end

redis.call('hincrby', KEYS[1],ARGV[2],-1)
redis.call('hincrby', KEYS[2], ARGV[3], 1)
return 0
