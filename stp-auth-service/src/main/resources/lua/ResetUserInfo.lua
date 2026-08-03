-- KEYS[1]: 旧 access session key
-- KEYS[2]: 旧 refresh session key
-- KEYS[3]: access token key (username->access, 新旧共用)
-- KEYS[4]: refresh token key (username->refresh, 新旧共用)
-- KEYS[5]: 新 access session key
-- KEYS[6]: 新 refresh session key
-- ARGV[1]: access 过期秒数
-- ARGV[2]: 新 access token
-- ARGV[3]: refresh 过期秒数
-- ARGV[4]: 新 refresh token
-- ARGV[5]: 用户信息 JSON


redis.call('del', KEYS[1], KEYS[2], KEYS[3], KEYS[4])           -- 只删除旧 session
if ARGV[5] ~= nil and ARGV[5] ~= '' then
    redis.call('set', KEYS[3], ARGV[2], 'EX', ARGV[1])
    redis.call('set', KEYS[4], ARGV[4], 'EX', ARGV[3])
    redis.call('set', KEYS[5], ARGV[5], 'EX', ARGV[1])
    redis.call('set', KEYS[6], ARGV[5], 'EX', ARGV[3])

end
return 1