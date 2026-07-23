package com.summit.stp.rank_board.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.summit.stp.rank_board.domain.model.BoardType;
import com.summit.stp.rank_board.domain.model.RankBoard;
import com.summit.stp.rank_board.domain.repository.RankRepository;
import com.summit.stp.rank_board.infrastructure.persistence.mapper.CreatorRankMapper;
import com.summit.stp.rank_board.infrastructure.persistence.mapper.PostRankMapper;
import com.summit.stp.rank_board.infrastructure.persistence.mapper.TopicRankMapper;
import com.summit.stp.rank_board.infrastructure.persistence.po.CreatorRankPO;
import com.summit.stp.rank_board.infrastructure.persistence.po.PostRankPO;
import com.summit.stp.rank_board.infrastructure.persistence.po.TopicRankPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class RankRepositoryImpl implements RankRepository {
    private final CreatorRankMapper creatorRankMapper;
    private final PostRankMapper postRankMapper;
    private final TopicRankMapper topicRankMapper;

    @Override
    public List<RankBoard> queryCreatorRank(Integer size, LocalDate dateBack) {
        LambdaQueryWrapper<CreatorRankPO> queryWrapper = new LambdaQueryWrapper<CreatorRankPO>()
                .eq(CreatorRankPO::getPeriodDate, dateBack)
                .orderByAsc(CreatorRankPO::getRank)
                .last("LIMIT " + size);
        return creatorRankMapper.selectList(queryWrapper).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<RankBoard> queryPostRank(Integer size) {
        LambdaQueryWrapper<PostRankPO> queryWrapper = new LambdaQueryWrapper<PostRankPO>()
                .orderByDesc(PostRankPO::getPeriodDate)
                .orderByAsc(PostRankPO::getRank)
                .last("LIMIT " + size);
        return postRankMapper.selectList(queryWrapper).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<RankBoard> queryTopicRank(Integer size) {
        LambdaQueryWrapper<TopicRankPO> queryWrapper = new LambdaQueryWrapper<TopicRankPO>()
                .orderByDesc(TopicRankPO::getPeriodDate)
                .orderByAsc(TopicRankPO::getRank)
                .last("LIMIT " + size);
        return topicRankMapper.selectList(queryWrapper).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insertCreatorRank(List<RankBoard> rankBoardList) {
        if (rankBoardList == null || rankBoardList.isEmpty()) {
            return;
        }
        List<LocalDate> periodDates = rankBoardList.stream()
                .map(this::toPeriodDate)
                .distinct()
                .toList();
        creatorRankMapper.delete(new LambdaQueryWrapper<CreatorRankPO>()
                .in(CreatorRankPO::getPeriodDate, periodDates));
        List<CreatorRankPO> list = rankBoardList.stream()
                .map(this::toCreatorPO)
                .toList();
        creatorRankMapper.insert(list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insertPostRank(List<RankBoard> rankBoardList) {
        if (rankBoardList == null || rankBoardList.isEmpty()) {
            return;
        }
        List<LocalDate> periodDates = rankBoardList.stream()
                .map(this::toPeriodDate)
                .distinct()
                .toList();
        postRankMapper.delete(new LambdaQueryWrapper<PostRankPO>()
                .in(PostRankPO::getPeriodDate, periodDates));
        rankBoardList.stream()
                .map(this::toPostPO)
                .forEach(postRankMapper::insert);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insertTopicRank(List<RankBoard> rankBoardList) {
        if (rankBoardList == null || rankBoardList.isEmpty()) {
            return;
        }
        List<LocalDate> periodDates = rankBoardList.stream()
                .map(this::toPeriodDate)
                .distinct()
                .toList();
        topicRankMapper.delete(new LambdaQueryWrapper<TopicRankPO>()
                .in(TopicRankPO::getPeriodDate, periodDates));
        rankBoardList.stream()
                .map(this::toTopicPO)
                .forEach(topicRankMapper::insert);
    }

    private CreatorRankPO toCreatorPO(RankBoard rankBoard) {
        return CreatorRankPO.builder()
                .userId(rankBoard.getEntityId())
                .score(BigDecimal.valueOf(rankBoard.getScore() == null ? 0D : rankBoard.getScore()))
                .rank(rankBoard.getRank())
                .periodDate(toPeriodDate(rankBoard))
                .updateTime(rankBoard.getUpdateTime())
                .createTime(rankBoard.getUpdateTime())
                .build();
    }

    private PostRankPO toPostPO(RankBoard rankBoard) {
        return PostRankPO.builder()
                .postId(rankBoard.getEntityId())
                .score(BigDecimal.valueOf(rankBoard.getScore() == null ? 0D : rankBoard.getScore()))
                .rank(rankBoard.getRank())
                .periodDate(toPeriodDate(rankBoard))
                .updateTime(rankBoard.getUpdateTime())
                .createTime(rankBoard.getUpdateTime())
                .build();
    }

    private TopicRankPO toTopicPO(RankBoard rankBoard) {
        return TopicRankPO.builder()
                .tagId(rankBoard.getEntityId())
                .score(BigDecimal.valueOf(rankBoard.getScore() == null ? 0D : rankBoard.getScore()))
                .rank(rankBoard.getRank())
                .periodDate(toPeriodDate(rankBoard))
                .updateTime(rankBoard.getUpdateTime())
                .createTime(rankBoard.getUpdateTime())
                .build();
    }

    private RankBoard toDomain(CreatorRankPO entry) {
        return RankBoard.builder()
                .id(entry.getId())
                .name("创作者周榜")
                .entityId(entry.getUserId())
                .score(entry.getScore() == null ? 0D : entry.getScore().doubleValue())
                .rank(entry.getRank())
                .status(1)
                .weekStartDate(toTimestamp(entry.getPeriodDate()))
                .type(BoardType.CREATOR.getType())
                .bgImg(null)
                .build();
    }

    private RankBoard toDomain(PostRankPO entry) {
        return RankBoard.builder()
                .id(entry.getId())
                .name("热点榜")
                .entityId(entry.getPostId())
                .score(entry.getScore() == null ? 0D : entry.getScore().doubleValue())
                .rank(entry.getRank())
                .status(1)
                .weekStartDate(toTimestamp(entry.getPeriodDate()))
                .type(BoardType.POST.getType())
                .bgImg(null)
                .build();
    }

    private RankBoard toDomain(TopicRankPO entry) {
        return RankBoard.builder()
                .id(entry.getId())
                .name("话题榜")
                .entityId(entry.getTagId())
                .score(entry.getScore() == null ? 0D : entry.getScore().doubleValue())
                .rank(entry.getRank())
                .status(1)
                .weekStartDate(toTimestamp(entry.getPeriodDate()))
                .type(BoardType.TOPIC.getType())
                .bgImg(null)
                .build();
    }

    private LocalDate toPeriodDate(RankBoard rankBoard) {
        if (rankBoard.getWeekStartDate() == null) {
            return LocalDate.now();
        }
        return rankBoard.getWeekStartDate().toLocalDateTime().toLocalDate();
    }

    private Timestamp toTimestamp(LocalDate periodDate) {
        if (periodDate == null) {
            return null;
        }
        return Timestamp.valueOf(periodDate.atStartOfDay());
    }
}
