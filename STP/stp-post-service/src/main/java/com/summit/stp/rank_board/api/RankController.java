package com.summit.stp.rank_board.api;

import com.summit.stp.rank_board.application.service.RankService;
import com.summit.stp.rank_board.application.vo.CreatorRankVO;
import com.summit.stp.rank_board.application.vo.PostRankVO;
import com.summit.stp.rank_board.application.vo.TopicRankVO;
import com.summit.stp.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/rank")
@RequiredArgsConstructor
public class RankController {
    private final RankService rankService;

    @GetMapping("/hot-search/top-hot")
    @Operation(summary = "查询热点榜")
    public Result<List<PostRankVO>> queryRankList(@RequestParam(defaultValue = "10") Integer size) {
        return rankService.queryHotRankList(size, 1);
    }

    @Operation(summary = "查询创作者热榜")
    @GetMapping("/content/creator/top-list")
    public Result<List<CreatorRankVO>> queryCreatorRankList(@RequestParam(defaultValue = "10") Integer size) {
        return rankService.queryCreatorRankList(size, 1);
    }

    @GetMapping("/content/subject/hot-subject")
    @Operation(summary = "查询话题热榜")
    public Result<List<TopicRankVO>> querySubjectRankList(@RequestParam(defaultValue = "10") Integer size) {
        return rankService.querySubjectRankList(size, 1);
    }
}
