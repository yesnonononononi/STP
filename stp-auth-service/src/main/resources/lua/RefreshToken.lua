-- Keys[1] access-token-key  token:access:uname
-- Keys[2] access-session-key session:access:token
-- Keys[3] refresh-token-key    token:refresh:uname
-- Keys[4] refresh-session-key  session:refresh:refresh_token
-- Keys[5] session-access-new_token-key session:access:new_token
-- ARGV[1] access-token过期时间
-- ARGV[2] refresh-token过期时间
-- ARGV[3] refresh_token
-- ARGV[4] userSession-json
-- ARGV[5] new_access_token
-- retrun 1 成功
-- retrun 2 旧refresh-token不存在


local token_access_uname_key = KEYS[1]
local session_access_token_key = KEYS[2]
local token_refresh_uname_key = KEYS[3]
local session_refresh_token = KEYS[4]
local session_access_new_token_key = KEYS[5]
local access_token_expire = ARGV[1]
local refresh_token_expire =  ARGV[2]
local refresh_token = ARGV[3]
local user_session = ARGV[4]
local new_access_token = ARGV[5]

local real_refresh_token = redis.call('get',token_refresh_uname_key)
-- 旧refresh-token是否存在 不存在->2
if  real_refresh_token == nil or real_refresh_token ~= refresh_token then
    return 2
end

-- 删除旧的 token-access session-access 映射
redis.call('del',session_access_token_key)

-- 保存新 token-access session-access 映射
redis.call('set',token_access_uname_key,new_access_token,'EX',access_token_expire)
redis.call('set',session_access_new_token_key,user_session,'EX',access_token_expire)

-- 刷新 refresh-token 的过期时间
redis.call('set', token_refresh_uname_key, refresh_token, 'EX', refresh_token_expire)
redis.call('set', session_refresh_token, user_session, 'EX', refresh_token_expire)

return 1