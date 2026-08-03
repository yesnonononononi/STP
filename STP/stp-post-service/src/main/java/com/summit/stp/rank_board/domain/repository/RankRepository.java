package com.summit.stp.rank_board.domain.repository;

import com.summit.stp.rank_board.domain.model.RankBoard;

import java.time.LocalDate;
import java.util.List;

public interface RankRepository {
    /**
     * 查询创作者排行榜
     * @param dateBack 周期时间
     * @param size 查询数量
     * @return 排行榜
     */
    List<RankBoard> queryCreatorRank(Integer size, LocalDate dateBack);
    List<RankBoard> queryPostRank(Integer size, LocalDate dateBack);
    List<RankBoard> queryTopicRank(Integer size, LocalDate dateBack);
    
    void insertCreatorRank(List<RankBoard> rankBoardList);
    void insertPostRank(List<RankBoard> rankBoardList);
    void insertTopicRank(List<RankBoard> rankBoardList);
}
