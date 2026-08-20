package com.summit.stp.post.application.support;

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
        int days = "30d".equalsIgnoreCase(period) ? 30 : 7;
        PostContentStat stat = postRepository.countContentStats(days);
        return PostContentStatsVO.builder()
                .dates(stat != null && stat.getDates() != null ? stat.getDates() : Collections.emptyList())
                .postCountList(stat != null && stat.getPostCountList() != null ? stat.getPostCountList() : Collections.emptyList())
                .commentCountList(stat != null && stat.getCommentCountList() != null ? stat.getCommentCountList() : Collections.emptyList())
                .blockedCountList(stat != null && stat.getBlockedCountList() != null ? stat.getBlockedCountList() : Collections.emptyList())
                .build();
    }
}
