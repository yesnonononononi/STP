package com.summit.stp.post.api;

import com.summit.stp.common.application.api.result.Result;
import com.summit.stp.post.api.vo.stats.PostContentStatsVO;
import com.summit.stp.post.application.support.PostDashSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/post/internal/stats")
@RequiredArgsConstructor
public class PostInternalStatsController {

    private final PostDashSupport postDashSupport;

    @GetMapping("/content")
    public Result<PostContentStatsVO> getContentStats(@RequestParam("period") String period) {
        return Result.success(postDashSupport.getContentStats(period));
    }
}
