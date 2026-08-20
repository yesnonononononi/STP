package com.summit.stp.comment.comment.infrastructure.persistence.scheduler;

import com.summit.stp.comment.comment.domain.model.Comment;
import com.summit.stp.comment.comment.domain.repository.CommentRepository;
import com.summit.stp.comment.comment.infrastructure.constants.CommentConstants;
import com.summit.stp.common.util.DistributedLockUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CommentHotScoreScheduler {
    private final DistributedLockUtil distributedLockUtil;
    private final CommentRepository commentRepository;

    @Scheduled(cron = "0 0/5 * * * * ")
    public void conduct(){
        distributedLockUtil.executeWithLock(CommentConstants.Cache.HOT_SCORE_LOCK, this::mainLogic);
    }
    public void mainLogic(){
        //1,查询最近活跃的评论
        List<Comment> comments;
        Timestamp updateTime = null;
        Long lastId = null;
        LocalDateTime deadline = LocalDateTime.now().minusDays(7);
        while(!((comments=commentRepository.queryActiveComments(deadline,updateTime, lastId))).isEmpty()){
            comments.forEach(Comment::updateScore);
            Comment last = comments.getLast();
            updateTime = last.getUpdateTime();
            lastId = last.getId();
            commentRepository.batchUpdateHs(comments);
        }


    }
}
