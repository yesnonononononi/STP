package com.summit.stp.shared.handler;

import com.summit.stp.shared.ThreadContext.UserHolder;
import com.summit.stp.userAuth.domain.model.UserSession;
import com.summit.stp.userAuth.domain.repository.TokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class Interceptor implements HandlerInterceptor {
    
    private final TokenRepository tokenRepository;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        // 放行登录和刷新接口
        String uri = request.getRequestURI();
        if (uri.contains("/user-auth/login") || uri.contains("/user-auth/refresh-token")) {
            return true;
        }

        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
             String token = authorization.substring(7);
             Optional<UserSession> sessionOpt = tokenRepository.findSessionByToken(token);
             
             if (sessionOpt.isPresent()) {
                 // 将用户信息存入 Request 作用域，或使用 ThreadLocal
                 UserHolder.setUser(sessionOpt.get());
                 return true;
             }
        }
        
        // 鉴权失败
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return false;
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, @Nullable Exception ex) throws Exception {
        UserHolder.clear();
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
