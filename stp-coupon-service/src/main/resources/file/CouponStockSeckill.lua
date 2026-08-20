-- KEYS[1] : 总库存Hash Key (coupon:activity:stock)  -> field=activityId, value=stock
-- KEYS[2] : 用户领券记录Hash Key (user_limit:userId)  -> field=couponId, value=count
-- ARGV[1] : 活动id (activityId)
-- ARGV[2] : 优惠券id (couponId)
if (redis.call("HEXISTS", KEYS[1], ARGV[1]) == 0) then
    return -3
end
local stock = redis.call('hget', KEYS[1], ARGV[1]);

if (tonumber(stock) == nil or (tonumber(stock) and tonumber(stock) <= 0)) then
    return -1
end

-- 检查用户可领取上限 (系统统一设定一人只能领 1 张)
local current = redis.call('hget', KEYS[2], ARGV[2]);
local currentNum = tonumber(current) or 0
if currentNum >= 1 then
    return -2   -- 已达个人上限 (一人仅可领 1 张)
end

redis.call('hincrby', KEYS[1], ARGV[1], -1)
redis.call('hincrby', KEYS[2], ARGV[2], 1)
return 0
