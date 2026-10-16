package com.summit.stp.post.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

/**
 * 帖子模块 Redis Lua 脚本配置。
 *
 * <p>集中注册脚本 Bean，保证脚本在应用启动时完成加载与校验，供缓存操作类复用。</p>
 */
@Configuration
public class PostRedisScriptConfiguration {

    /**
     * 帖子互动（点赞/收藏）原子切换脚本。
     *
     * <p>脚本内以单次原子执行完成"读判定 + 改写"，消除先读后写的竞态，
     * 保证点赞/收藏计数恒等于互动集合的真实规模。</p>
     *
     * @return 返回值为 Long 的切换脚本
     */
    @Bean
    public DefaultRedisScript<Long> postInteractionToggleScript() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("file/post_interaction_toggle.lua")));
        script.setResultType(Long.class);
        return script;
    }
}
