-- 帖子互动（点赞/收藏）原子切换脚本
-- 将"读判定 + 改写"合并为单次原子操作，保证不变量：计数恒等于互动集合真实规模。
--
-- KEYS[1] : 互动集合 ZSet key（post:like:{postId} / post:collect:{postId}）
-- KEYS[2] : 帖子详情 Hash key（post:detail:{postId}）
-- KEYS[3] : 计数 Hash field 名（likeCount / collectCount，用 key 序列化器保证原始字节）
-- ARGV[1] : 互动用户 member（经 RedisTemplate value 序列化器序列化的 userId 字节）
--
-- 返回值：1 表示本次为首次互动（原先 score 不存在），0 表示其他情况，与原 Java 语义一致。
local setKey = KEYS[1]
local postKey = KEYS[2]
local countField = KEYS[3]
local member = ARGV[1]

local score = redis.call('ZSCORE', setKey, member)

if (not score) or (tonumber(score) == 0) then
    -- 首次互动或处于未互动态：加入集合并自增计数
    redis.call('ZADD', setKey, 1, member)
    redis.call('HINCRBY', postKey, countField, 1)
    if not score then
        return 1
    end
    return 0
elseif tonumber(score) == 1 then
    -- 已互动：移出集合并自减计数，计数不为负（<0 归 0）
    redis.call('ZREM', setKey, member)
    local newCount = redis.call('HINCRBY', postKey, countField, -1)
    if tonumber(newCount) < 0 then
        redis.call('HSET', postKey, countField, 0)
    end
    return 0
end
return 0
