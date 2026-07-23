package com.summit.stp.shared.handler;

import cn.hutool.core.lang.UUID;
import com.github.xiaoymin.knife4j.core.util.StrUtil;
import tools.jackson.databind.json.JsonMapper;
import com.summit.stp.shared.ThreadContext.UserHolder;
import com.summit.stp.userAuth.infrastructure.constants.UserAuthConstants;
import com.summit.stp.shared.result.Result;
import com.summit.stp.shared.util.IpUtil;
import com.summit.stp.userAuth.domain.model.UserSession;
import com.summit.stp.userAuth.domain.repository.TokenRepository;
import io.netty.util.internal.StringUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import com.summit.stp.shared.annotation.Login;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Optional;

import static jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@Component
public class Interceptor implements HandlerInterceptor {

    @Autowired(required = false)
    private TokenRepository tokenRepository;
    
    @Autowired
    private JsonMapper objectMapper;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        // 校验内部微服务RPC接口的合法性，防止外部直接调用
        String uri = request.getRequestURI();
        if (uri.contains("/internal/")) {
            String innerHeader = request.getHeader("X-Internal-Request");
            if (!"true".equals(innerHeader)) {
                log.warn("【系统拦截】检测到非法外部调用内部RPC接口，URI: {}, IP: {}", uri, request.getRemoteAddr());
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json;charset=UTF-8");
                try {
                    Result<Void> errorResult = Result.error(403, "禁止外部访问内部接口");
                    response.getWriter().write(objectMapper.writeValueAsString(errorResult));
                } catch (IOException e) {
                    log.error("【系统拦截】写入403响应异常", e);
                }
                return false;
            }
        }

        UserSession userSession = null;
        String sessionHeader = request.getHeader("X-User-Session");
        if (sessionHeader != null && !sessionHeader.isEmpty()) {
            try {
                byte[] decoded = java.util.Base64.getDecoder().decode(sessionHeader);
                userSession = objectMapper.readValue(decoded, UserSession.class);
            } catch (Exception e) {
                log.error("【拦截器】解析透传 X-User-Session 失败", e);
            }
        }
        
        if (userSession == null) {
            String userIdStr = request.getHeader("X-User-Id");
            if (!StrUtil.isBlank(userIdStr) && !"null".equalsIgnoreCase(userIdStr)) {
                String username = request.getHeader("X-User-Name");
                String adminStr = request.getHeader("X-User-Admin");
                String tokenTypeStr = request.getHeader("X-User-Token-Type");
                String token = request.getHeader("X-User-Token");
                
                userSession = UserSession.builder()
                        .id(Long.valueOf(userIdStr))
                        .username(username)
                        .admin(adminStr != null ? Integer.valueOf(adminStr) : 0)
                        .tokenType(tokenTypeStr != null ? UserSession.TokenType.valueOf(tokenTypeStr) : UserSession.TokenType.ACCESS)
                        .token(token)
                        .build();
            }
        }
        

        
        // 登录态验证失败或未携带 Token 时，平滑降级为游客身份处理
        if (userSession == null) {
            userSession = handleGuest(request, response);
        }
        
        // 编排：执行登录校验逻辑
        if (isLoginRequired(handler)) {
            if (userSession.getTokenType() != UserSession.TokenType.ACCESS) {
                sendUnauthorizedResponse(response);
                return false;
            }
        }

        UserHolder.setUser(userSession);
        return true;
    }

    private boolean isLoginRequired(Object handler) {
        if (handler instanceof HandlerMethod handlerMethod) {
            if (handlerMethod.getMethodAnnotation(Login.class) != null) {
                return true;
            }
            return handlerMethod.getBeanType().getAnnotation(Login.class) != null;
        }
        return false;
    }

    private void sendUnauthorizedResponse(HttpServletResponse response) {
        response.setStatus(SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        try {
            Result<Void> errorResult = Result.error(401, "未登录或登录已过期");
            response.getWriter().write(objectMapper.writeValueAsString(errorResult));
        } catch (IOException e) {
            log.error("【鉴权模块】写入401响应失败", e);
        }
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, @Nullable Exception ex) throws Exception {
        UserHolder.clear();
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }

    private UserSession handleGuest(HttpServletRequest request, HttpServletResponse response) {
        Optional<UserSession> sessionByToken = Optional.empty();
        String did = getDeviceIdFromCookie(request);
        if (StringUtil.isNullOrEmpty(did)) {
            did = UUID.fastUUID().toString();
            Cookie cookie = new Cookie("device_id", did);
            cookie.setMaxAge((int) (UserAuthConstants.Business.GUEST_TOKEN_TTL / 1000)); // 6天
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            response.addCookie(cookie);
        } else if (tokenRepository != null) {
            sessionByToken = tokenRepository.findSessionByToken(did, UserSession.TokenType.GUEST);
        }
        if (sessionByToken.isEmpty()) {
            String ipAddr = IpUtil.getIpAddr(request);
            UserSession user = buildGuestSession(ipAddr, did);
            sessionByToken = Optional.of(user);
            UserHolder.setUser(user);
            if (tokenRepository != null) {
                tokenRepository.saveSession(did, user, UserAuthConstants.Business.GUEST_TOKEN_TTL / 1000, UserSession.TokenType.GUEST);
            }
        }
        return sessionByToken.get();
    }

    private String getDeviceIdFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("device_id".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private UserSession buildGuestSession(String ip, String did) {
        return UserSession.builder()
                .token(did)
                .onlineStatus("ONLINE")
                .ip(ip)
                .tokenType(UserSession.TokenType.GUEST)
                .build();
    }
}
