package com.summit.stp.common.auth.aspect;

import com.summit.stp.common.annotation.Admin;
import com.summit.stp.common.annotation.Login;
import com.summit.stp.common.application.domain.exception.AuthException;
import com.summit.stp.common.application.domain.model.UserSession;
import com.summit.stp.common.auth.UserHolder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * 鉴权 AOP 切面
 * 拦截标注了 @Login 和 @Admin 的方法，进行声明式鉴权
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuthAspect {

    /**
     * 拦截 @Login 注解：校验用户是否已登录（ACCESS Token）
     */
    @Before(value = "@annotation(login) || @within(login)", argNames = "login,jp")
    public void checkLogin(Login login, @NonNull JoinPoint jp) {
        if (login == null) {
            login = (Login) getAnno(Login.class, jp);
        }
        UserSession session = getSession();
        if (session.getTokenType() != UserSession.TokenType.ACCESS) {
            log.warn("【鉴权】@Login 校验失败，当前 session tokenType={}", session.getTokenType());
            throw new AuthException(401, "请先登录");
        }
    }

    /**
     * 拦截 @Admin 注解：校验用户是否为管理员
     * 处理顺序：先验证登录 → 再验证管理员身份
     */
    @Before(value = "@annotation(admin) || @within(admin)", argNames = "admin,jp")
    public void checkAdmin(Admin admin, @NonNull JoinPoint jp) {
        if (admin == null) {
            admin = (Admin) getAnno(Admin.class, jp);
        }
        UserSession session = getSession();
        if (session.getTokenType() != UserSession.TokenType.ACCESS) {
            log.warn("【鉴权】@Admin 校验失败（未登录），tokenType={}", session.getTokenType());
            throw new AuthException(401, "请先登录");
        }
        if (!isAdmin(session, admin.order())) {
            log.warn("【鉴权】@Admin 校验失败（非管理员），userId={}", session.getId());
            throw new AuthException(403, "无管理员权限");
        }
    }

    private boolean isAdmin(UserSession session, int requireOrder) {
        // 登录时已通过远程调用将管理员等级/订单数 set 到 UserSession 中。
        // 若没有（值为 null），则判定为非管理员。
        Integer adminOrder = session.getAdmin();
        return adminOrder != null && adminOrder >= requireOrder;
    }

    private UserSession getSession() {
        try {
            return UserHolder.getUser();
        } catch (Exception e) {
            throw new AuthException(401, "请先登录");
        }
    }

    private Annotation getAnno(Class<? extends Annotation> clazz, JoinPoint jp) {
        // 1. 优先拿方法上的注解
        MethodSignature signature = (MethodSignature) jp.getSignature();
        Method method = signature.getMethod();
        Annotation anno = AnnotatedElementUtils.findMergedAnnotation(method, clazz);

        // 2. 方法上没有，拿类上的注解（支持 Spring 的@AliasFor等合并）
        if (anno == null) {
            Class<?> targetClass = jp.getTarget().getClass();
            anno = AnnotatedElementUtils.findMergedAnnotation(targetClass, clazz);
        }
        return anno;
    }
}
