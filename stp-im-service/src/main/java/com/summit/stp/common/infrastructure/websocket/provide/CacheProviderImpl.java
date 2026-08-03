package com.summit.stp.common.infrastructure.websocket.provide;

import com.summit.stp.common.constants.ImConstants;
import com.summit.stp.common.constants.UserAuthConstants;
import com.summit.stp.common.infrastructure.properties.ImProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Component
public class CacheProviderImpl implements CacheProvider {

    private final StringRedisTemplate stringRedisTemplate;
    private final ImProperties imProperties;


    @Override
    public void setOnline(Long uid, String ticket) {
        if (uid == null || ticket == null) {
            return;
        }
        String key = buildOnlineKey(uid);
        stringRedisTemplate.opsForValue().set(key, ticket, imProperties.getTtl(), TimeUnit.HOURS);
    }

    @Override
    public String getTicket(Long uid) {
        if (uid == null) {
            return null;
        }
        return stringRedisTemplate.opsForValue().get(buildOnlineKey(uid));
    }

    @Override
    public void removeOnline(Long uid, String ticket) {
        if (uid == null || ticket == null) {
            return;
        }
        String key = buildOnlineKey(uid);
        String currentTicket = stringRedisTemplate.opsForValue().get(key);
        if (ticket.equals(currentTicket)) {
            stringRedisTemplate.delete(key);
        }
    }

    @Override
    public boolean auth(String token) {
        if (token == null) {
            return false;
        }
        String key = UserAuthConstants.Cache.ACCESS_SESSION + token;
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(key))) {
            stringRedisTemplate.expire(key, imProperties.getTtl(), TimeUnit.HOURS);
            return true;
        }
        return false;
    }

    private String buildOnlineKey(Long uid) {
        return ImConstants.Cache.ONLINE_KEY + uid;
    }
}
