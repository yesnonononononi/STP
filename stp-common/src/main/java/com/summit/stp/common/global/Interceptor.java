package com.summit.stp.common.global;

import cn.hutool.core.lang.UUID;
import com.github.xiaoymin.knife4j.core.util.StrUtil;
import com.summit.stp.common.ThreadContext.UserHolder;
import com.summit.stp.common.annotation.Login;
import com.summit.stp.common.application.domain.model.UserSession;
import com.summit.stp.common.auth.GuestSessionRepository;
import com.summit.stp.common.constants.UserAuthConstants;
import com.summit.stp.common.result.Result;
import com.summit.stp.common.util.IpUtil;
import io.netty.util.internal.StringUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.util.Optional;

import static jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

@Slf4j
@Component
@RequiredArgsConstructor
public class Interceptor implements HandlerInterceptor {

    private final JsonMapper objectMapper;
    private final GuestSessionRepository guestSessionRepository;

    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler
    ) {
        // 校验内部微服务 RPC 接口的合法性，防止外部直接调用。
        String uri = request.getRequestURI();
        if (uri.contains("/internal/")) {
            String innerHeader = request.getHeader("X-Internal-Request");
            if (!"true".equals(innerHeader)) {
                log.warn("【系统拦截】检测到非法外部调用内部 RPC 接口，URI: {}, IP: {}", uri, request.getRemoteAddr());
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json;charset=UTF-8");
                try {
                    Result<Void> errorResult = Result.error(403, "禁止外部访问内部接口");
                    response.getWriter().write(objectMapper.writeValueAsString(errorResult));
                } catch (IOException e) {
                    log.error("【系统拦截】写入 403 响应异常", e);
                }
                return false;
            }
        }

        UserSession userSession = resolveUserSession(request);
        if (userSession == null) {
            userSession = handleGuest(request, response);
        }

        if (isLoginRequired(handler)
                && userSession.getTokenType() != UserSession.TokenType.ACCESS) {
            sendUnauthorizedResponse(response);
            return false;
        }

        UserHolder.setUser(userSession);
        return true;
    }

    private UserSession resolveUserSession(HttpServletRequest request) {
        String sessionHeader = request.getHeader("X-User-Session");
        if (sessionHeader != null && !sessionHeader.isEmpty()) {
            try {
                byte[] decoded = java.util.Base64.getDecoder().decode(sessionHeader);
                return objectMapper.readValue(decoded, UserSession.class);
            } catch (Exception e) {
                log.error("【拦截器】解析透传 X-User-Session 失败", e);
            }
        }

        String userIdStr = request.getHeader("X-User-Id");
        if (StrUtil.isBlank(userIdStr) || "null".equalsIgnoreCase(userIdStr)) {
            return null;
        }

        try {
            String adminStr = request.getHeader("X-User-Admin");
            String tokenTypeStr = request.getHeader("X-User-Token-Type");
            return UserSession.builder()
                    .id(Long.valueOf(userIdStr))
                    .username(request.getHeader("X-User-Name"))
                    .admin(adminStr != null ? Integer.valueOf(adminStr) : 0)
                    .tokenType(tokenTypeStr != null
                            ? UserSession.TokenType.valueOf(tokenTypeStr)
                            : UserSession.TokenType.ACCESS)
                    .token(request.getHeader("X-User-Token"))
                    .build();
        } catch (RuntimeException e) {
            log.warn("【拦截器】解析网关用户信息失败", e);
            return null;
        }
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
            log.error("【鉴权模块】写入 401 响应失败", e);
        }
    }

    @Override
    public void afterCompletion(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler,
            @Nullable Exception ex
    ) throws Exception {
        UserHolder.clear();
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }

    private static final String DEVICE_ID_HEADER = "X-Device-Id";
    private static final String DEVICE_ID_COOKIE = "device_id";

    private UserSession handleGuest(HttpServletRequest request, HttpServletResponse response) {
        // 优先读取前端持久化的设备 ID，Cookie 作为兼容旧客户端的回退方案。
        String did = getDeviceId(request);
        if (StringUtil.isNullOrEmpty(did)) {
            did = UUID.fastUUID().toString();
            addDeviceIdCookie(response, did);
        }

        Optional<UserSession> existingSession = guestSessionRepository.findByToken(did);
        if (existingSession.isPresent()) {
            return existingSession.get();
        }

        UserSession candidate = buildGuestSession(IpUtil.getIpAddr(request), did);
        // SET NX 保证并发请求/多实例下同一 device_id 只创建一个游客会话。
        return guestSessionRepository.saveIfAbsent(did, candidate).orElse(candidate);
    }

    private String getDeviceId(HttpServletRequest request) {
        String headerDeviceId = request.getHeader(DEVICE_ID_HEADER);
        if (isValidDeviceId(headerDeviceId)) {
            return headerDeviceId;
        }

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (DEVICE_ID_COOKIE.equals(cookie.getName()) && isValidDeviceId(cookie.getValue())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private boolean isValidDeviceId(String did) {
        return did != null && did.length() <= 64 && did.matches("[A-Za-z0-9_-]+");
    }

    private void addDeviceIdCookie(HttpServletResponse response, String did) {
        Cookie cookie = new Cookie(DEVICE_ID_COOKIE, did);
        cookie.setMaxAge((int) (UserAuthConstants.Business.GUEST_TOKEN_TTL / 1000));
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
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
