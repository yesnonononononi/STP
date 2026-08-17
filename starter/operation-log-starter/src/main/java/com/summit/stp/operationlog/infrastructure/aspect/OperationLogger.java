package com.summit.stp.operationlog.infrastructure.aspect;

import cn.hutool.core.util.StrUtil;
import com.summit.stp.common.application.api.result.Result;
import com.summit.stp.common.auth.UserHolder;
import com.summit.stp.common.application.domain.model.UserSession;
import com.summit.stp.common.util.SpelResolver;
import com.summit.stp.operationlog.annotation.OperationLog;
import com.summit.stp.operationlog.domain.model.Operation;
import com.summit.stp.operationlog.domain.repository.OperationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aop.aspectj.MethodInvocationProceedingJoinPoint;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.Instant;

@Slf4j
@RequiredArgsConstructor
@Aspect
@Component
public class OperationLogger {
    private final OperationLogRepository operationLogRepository;

    @AfterReturning(pointcut = "@annotation(operationLog)",returning = "result")
    public void method(@NonNull JoinPoint joinPoint, @NonNull OperationLog operationLog,@NonNull Object result) {
        if(result instanceof Result<?> res){
            if(!res.isSuccess())return;
        }

        log.info("【操作日志-切面】开始保存操作日志:{}", operationLog.description());
        Long entityId = null;
        String description = operationLog.description();
        Operation.OperationType type = operationLog.type();
        UserSession user = UserHolder.getUser();
        Long uid = user.getId();
        String username = user.getUsername();
        String expression = operationLog.entityId();
        if(StrUtil.isNotBlank(expression)) {
            try {
                entityId = SpelResolver.resolveExpr(joinPoint, expression, Long.class);
            } catch (Exception e) {
                log.error("【操作日志-切面】解析表达式失败:{}", expression, e);
            }
        }

        Operation operation = Operation.builder()
                .operation(description)
                .type(type)
                .createTime(Instant.now())
                .userId(uid)
                .entityId(entityId)
                .username(username)
                .build();
        try {
            operationLogRepository.save(operation);
            log.info("【操作日志-切面】保存操作日志成功,用户id:{},操作:{}", uid, description);
        } catch (Exception e) {
            log.error("【操作日志-切面】保存操作日志失败,用户id:{}", uid, e);
        }
    }



}
