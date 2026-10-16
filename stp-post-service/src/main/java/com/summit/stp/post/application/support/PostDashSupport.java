package com.summit.stp.post.application.support;

import com.summit.stp.common.application.domain.exception.ParameterException;
import com.summit.stp.post.api.vo.stats.PostContentStatsVO;
import com.summit.stp.post.domain.model.stats.PostContentStat;
import com.summit.stp.post.domain.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * 社区帖子模块 Dashboard 数据查询底层支撑组件
 * 纯粹依赖 PostRepository 仓储，屏蔽 Mapper 泄露
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PostDashSupport {

    private final PostRepository postRepository;

    public PostContentStatsVO getContentStats(String period) {
        log.info("【帖子模块】[DashSupport] 基于 PostRepository 查询社区内容与互动真实走势, 周期: {}", period);
        int days = resolveDays(period);
        PostContentStat stat = postRepository.countContentStats(days);
        return PostContentStatsVO.builder()
                .dates(stat != null && stat.getDates() != null ? stat.getDates() : Collections.emptyList())
                .postCountList(stat != null && stat.getPostCountList() != null ? stat.getPostCountList() : Collections.emptyList())
                .commentCountList(stat != null && stat.getCommentCountList() != null ? stat.getCommentCountList() : Collections.emptyList())
                .blockedCountList(stat != null && stat.getBlockedCountList() != null ? stat.getBlockedCountList() : Collections.emptyList())
                .build();
    }

    /**
     * 解析统计周期口径（#17）：支持 7d / 30d / 90d。
     * <p>空值默认 7 天并记录告警；非法值抛出参数异常，禁止静默按 7 天处理。</p>
     */
    private static int resolveDays(String period) {
        if (period == null || period.isBlank()) {
            log.warn("【帖子模块】[DashSupport] 统计周期为空，默认使用 7d");
            return 7;
        }
        switch (period.trim().toLowerCase()) {
            case "7d":
                return 7;
            case "30d":
                return 30;
            case "90d":
                return 90;
            default:
                throw new ParameterException("不支持的统计周期: " + period + "，可选值: 7d/30d/90d");
        }
    }
}
