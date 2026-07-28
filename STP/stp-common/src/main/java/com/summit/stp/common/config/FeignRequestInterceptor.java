package com.summit.stp.common.config;

import com.summit.stp.common.ThreadContext.UserHolder;
import com.summit.stp.common.application.domain.model.UserSession;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FeignRequestInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate template) {
        template.header("X-Internal-Request", "true");
        try {
            UserSession user = UserHolder.getUser();
            if (user.getId() != null) {
                template.header("X-User-Id", String.valueOf(user.getId()));
            }
            if (user.getUsername() != null) {
                template.header("X-User-Name", user.getUsername());
            }
            if (user.getAdmin() != null) {
                template.header("X-User-Admin", String.valueOf(user.getAdmin()));
            }
            if (user.getTokenType() != null) {
                template.header("X-User-Token-Type", user.getTokenType().name());
            }
            if (user.getToken() != null) {
                template.header("X-User-Token", user.getToken());
            }
        } catch (Exception e) {
            // UserHolder.getUser() 在没有登录态或游客状态发生异常时直接忽略
        }
    }
}
