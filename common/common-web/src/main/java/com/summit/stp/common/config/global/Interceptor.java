package com.summit.stp.common.config.global;

import cn.hutool.core.lang.UUID;
import com.github.xiaoymin.knife4j.core.util.StrUtil;
import com.summit.stp.common.auth.UserHolder;
import com.summit.stp.common.application.domain.model.UserSession;
import com.summit.stp.common.auth.GuestSessionRepository;
import com.summit.stp.common.constants.UserAuthConstants;
import com.summit.stp.common.application.api.result.Result;
import com.summit.stp.common.util.IpUtil;
import io.netty.util.internal.StringUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

/**
 * 请求拦截器（迁移自 stp-common）
 * 职责：
 * 1. 校验 /internal/ 接口防外部访问（X-Internal-Request 头）
 * 2. 解析网关透传的 UserSession，写入 UserHolder
 * 3. 处理游客 Session（device_id）
 * <p>
 */
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
        // 1. 校验内部微服务 RPC 接口的合法性，防止外部直接调用。
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

        // 2. 解析用户会话（网关透传）
        UserSession userSession = resolveUserSession(request);
        if (userSession == null) {
            // 3. 无已登录 Session，处理游客会话
            userSession = handleGuest(request, response);
        }

        UserHolder.setUser(userSession);


        return true;
    }


    /**
     * 解析用户会话
     */
    private UserSession resolveUserSession(HttpServletRequest request) {
        String sessionHeader = request.getHeader("X-User-Session");
        //1, 有session 则返回
        if (sessionHeader != null && !sessionHeader.isEmpty()) {
            try {
                byte[] decoded = java.util.Base64.getDecoder().decode(sessionHeader);
                return objectMapper.readValue(decoded, UserSession.class);
            } catch (Exception e) {
                log.error("【拦截器】解析透传 X-User-Session 失败", e);
            }
        }
        //2, 没session,意味着这是本次请求的第一道拦截器,初始化session
        String userIdStr = request.getHeader("X-User-Id");
        if (StrUtil.isBlank(userIdStr) || "null".equalsIgnoreCase(userIdStr)) {
            return null;
        }

        try {
            String adminStr = request.getHeader("X-User-Admin");
            Integer adminVal = Optional.ofNullable(adminStr)
                    .filter(StrUtil::isNotBlank)
                    .map(Integer::parseInt)
                    .orElse(null);
            String tokenTypeStr = request.getHeader("X-User-Token-Type");
            return UserSession.builder()
                    .id(Long.valueOf(userIdStr))
                    .username(request.getHeader("X-User-Name"))
                    .admin(adminVal)
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


    /**
     * 请求完成时执行(幂等游客会话)
     */
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

    /**
     * 处理游客用户
     */
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

    /**
     * 从请求头或 Cookie 中获取设备 ID
     */
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

    /**
     * 验证设备 ID 是否有效
     */

    private boolean isValidDeviceId(String did) {
        return did != null && did.length() <= 64 && did.matches("[A-Za-z0-9_-]+");
    }

    /**
     * 添加设备 ID Cookie
     */
    private void addDeviceIdCookie(HttpServletResponse response, String did) {
        Cookie cookie = new Cookie(DEVICE_ID_COOKIE, did);
        cookie.setMaxAge((int) (UserAuthConstants.Business.GUEST_TOKEN_TTL / 1000));
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
    }

    /**
     * 构建游客用户会话
     */
    private UserSession buildGuestSession(String ip, String did) {
        return UserSession.builder()
                .token(did)
                .ip(ip)
                .tokenType(UserSession.TokenType.GUEST)
                .build();
    }
}


