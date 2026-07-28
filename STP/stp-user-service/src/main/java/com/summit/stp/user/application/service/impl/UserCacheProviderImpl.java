package com.summit.stp.user.application.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.summit.stp.user.infrastructure.constants.UserConstants;
import com.summit.stp.user.application.service.UserCacheProvider;
import com.summit.stp.common.application.vo.UserSimpleVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


@Slf4j
@Service
@RequiredArgsConstructor
public class UserCacheProviderImpl implements UserCacheProvider {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public Map<Long, UserSimpleVO> batchGetUserSimpleVO(List<Long> userIds) {

        List<Object> users = redisTemplate.executePipelined(new SessionCallback<>() {
            @Override
            public Object execute(@NonNull RedisOperations operations) throws DataAccessException {
                for (Long userId : userIds) {
                    redisTemplate.opsForHash().entries(UserConstants.Cache.USER_DETAIL + userId);
                }
                return null;
            }
        });
        HashMap<Long, UserSimpleVO> result = new HashMap<>(userIds.size());
        if(users.stream().filter(Objects::nonNull).toList().isEmpty()){
            return result;
        }
        for (Object user : users) {
            if (user instanceof Map map && !map.isEmpty()) {
                UserSimpleVO userSimpleVO = BeanUtil.toBean(map, UserSimpleVO.class);
                if (userSimpleVO.getId() != null) {
                     result.put(userSimpleVO.getId(), userSimpleVO);
                }
            } else if (user != null) {
                log.error("【缓存批量获取用户】:{}", user);
            }
        }
        return result;
    }


    @Override
    public void batchSetUserSimpleVO(Map<Long, UserSimpleVO> userSimpleVOMap) {
        redisTemplate.executePipelined(new SessionCallback<>() {
            @Override
            public Object execute(@NonNull RedisOperations operations) throws DataAccessException {
                for (Map.Entry<Long, UserSimpleVO> entry : userSimpleVOMap.entrySet()) {
                    redisTemplate.opsForHash().putAll(UserConstants.Cache.USER_DETAIL + entry.getKey(), BeanUtil.beanToMap(entry.getValue(),new HashMap<>(), CopyOptions.create().ignoreNullValue().ignoreError().setConverter((type,val)-> val!= null ? val.toString() : null)));
                    redisTemplate.expire(UserConstants.Cache.USER_DETAIL + entry.getKey(), UserConstants.Business.CACHE_TTL, TimeUnit.DAYS);
                }
                return null;
            }
        });
    }
}
