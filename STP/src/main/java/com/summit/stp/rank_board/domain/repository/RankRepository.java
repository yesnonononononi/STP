package com.summit.stp.rank_board.domain.repository;

import com.summit.stp.rank_board.domain.model.RankBoard;

import java.util.List;

public interface RankRepository {
    List<RankBoard> queryCreatorRank(Integer size);
    List<RankBoard> queryPostRank(Integer size);
    List<RankBoard> queryTopicRank(Integer size);
    
    void insertCreatorRank(List<RankBoard> rankBoardList);
    void insertPostRank(List<RankBoard> rankBoardList);
    void insertTopicRank(List<RankBoard> rankBoardList);
}
