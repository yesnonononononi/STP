package com.summit.stp.member.infrastructure.annotation.handler;

import com.summit.stp.common.ThreadContext.UserHolder;
import com.summit.stp.common.application.domain.exception.BusinessException;
import com.summit.stp.common.result.Result;
import com.summit.stp.member.domain.model.MemberType;
import com.summit.stp.member.domain.model.UserMember;
import com.summit.stp.member.infrastructure.annotation.VIP;
import com.summit.stp.member.infrastructure.persistence.UserMemberRepositoryImpl;
import com.summit.stp.user.infrastructure.constants.UserConstants;
import io.lettuce.core.RedisException;
import io.netty.util.internal.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
@Component
@RequiredArgsConstructor
@Aspect
public class VipHandler {


    private final UserMemberRepositoryImpl userMemberRepositoryImpl;
    private final StringRedisTemplate stringRedisTemplate;
    private final JsonMapper objectMapper;


    @Around("@annotation(vip)")
    public Object handleVip(ProceedingJoinPoint pjp, VIP vip) throws Throwable {

        //1,查状态,到期时间
        boolean isAccessible = checkVip(UserHolder.getUser().getId(), vip.type());
        if (isAccessible) {
            return pjp.proceed();
        }
        return Result.error("请开通会员");
    }

    private boolean checkVip(Long userId, MemberType type) {
        try {
            String objStr = stringRedisTemplate.opsForValue().get(UserConstants.Cache.STATE_CACHE + userId);
            if(StringUtil.isNullOrEmpty(objStr)){
                return false;
            }
            MemberType memberType = objectMapper.readValue(objStr, MemberType.class);
            if(memberType == null){
                log.info("【会员校验】 未获取到用户会员信息");
                return false;
            };
            return type.getPriority() > memberType.getPriority();
        }catch (RedisException e){
            log.error("【会员校验】缓存获取用户会员信息失败:{}", userId);
            try {
                UserMember userMember = getUserMemberFromDb(userId);
                if (userMember.isMemberActive()) {
                    MemberType curType = userMember.getMemberType();
                    if (curType == null) throw new RuntimeException("未获取到用户会员类型");
                    return curType.getPriority() >= type.getPriority();
                }
                return false;

            } catch (Exception exception) {
                log.error("【会员校验】db获取用户会员信息失败:{}", userId,exception);
                throw new BusinessException("会员校验失败,请稍后重试");
            }
        }

    }

    private UserMember getUserMemberFromDb(Long userId) {
        return userMemberRepositoryImpl.queryUserMemberByUserId(userId);
    }


}
