package com.summit.stp.rank_board.application.service;

import com.summit.stp.common.application.api.result.Result;
import com.summit.stp.rank_board.application.vo.CreatorRankVO;
import com.summit.stp.rank_board.application.vo.PostRankVO;
import com.summit.stp.rank_board.application.vo.TopicRankVO;

import java.util.List;

public interface RankService {

    Result<List<PostRankVO>> queryHotRankList(Integer size, Integer status);

    Result<List<CreatorRankVO>> queryCreatorRankList(Integer size, Integer status);

    Result<List<TopicRankVO>> querySubjectRankList(Integer size, Integer status);

}
