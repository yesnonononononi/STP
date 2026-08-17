package com.summit.stp.common.util;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;
import org.jspecify.annotations.Nullable;
import org.springframework.aop.aspectj.MethodInvocationProceedingJoinPoint;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import java.lang.reflect.Method;



public class SpelResolver {
    private final static  SpelExpressionParser spelExpressionParser = new SpelExpressionParser();
    private final static DefaultParameterNameDiscoverer discoverer = new DefaultParameterNameDiscoverer();

    public static Object resolverExp(String expression, Method method, Object[] args) {
        MethodBasedEvaluationContext context = new MethodBasedEvaluationContext(null, method, args, discoverer);
        return spelExpressionParser.parseRaw(expression).getValue(context);
    }

    /**
     * 解析spel
     * @param joinPoint 切入点
     * @param expression 表达式
     * @return 解析结果
     */
    public static <T> T resolveExpr(JoinPoint joinPoint, String expression, Class<T> type) {
        if (joinPoint instanceof MethodInvocationProceedingJoinPoint jp) {
            @Nullable Object[] args = jp.getArgs();

            Signature signature = jp.getSignature();

            if (signature instanceof MethodSignature methodSignature) {
                Method method = methodSignature.getMethod();
                Object res = SpelResolver.resolverExp(expression, method, args);
                if(type.isInstance(res)){
                    return type.cast(res);
                }
                throw new ClassCastException(String.format("不能转化 %s 为 %s 类型",res.getClass(), type));
            }
        }
        return null;
    }
}
