package com.summit.stp.comment.admin.application.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.summit.stp.comment.admin.application.command.CommentQueryCommand;
import com.summit.stp.comment.admin.application.service.AdminCommentService;
import com.summit.stp.comment.admin.application.vo.AdminCommentVO;
import com.summit.stp.comment.admin.domain.exception.NoSuchCommentException;
import com.summit.stp.comment.comment.domain.model.Comment;
import com.summit.stp.comment.comment.domain.repository.CommentRepository;
import com.summit.stp.common.application.api.result.PageResult;
import com.summit.stp.common.application.api.result.Result;
import com.summit.stp.common.util.DistributedLockUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminCommentServiceImpl implements AdminCommentService {

    /** 管理端写操作分布式锁等待时间（秒） */
    private static final long LOCK_WAIT_SECONDS = 3L;
    /** 管理端写操作分布式锁租约时间（秒） */
    private static final long LOCK_LEASE_SECONDS = 10L;

    private final CommentRepository commentRepository;
    private final DistributedLockUtil distributedLockUtil;

    @Override
    public Result<PageResult<List<AdminCommentVO>>> listBy(CommentQueryCommand command) {
        Integer page = Objects.requireNonNullElse(command.getPage(),1);
        Integer pageSize = Objects.requireNonNullElse(command.getPageSize(),10);
        String keyword = command.getKeyword();
        Integer status = command.getStatus();
        Page<Comment> p = commentRepository.queryPage(pageSize,page,keyword,status);
        return Result.success(new PageResult<>(p.getCurrent(), p.getTotal(), p.getRecords().stream().map(this::toVO).toList()));
    }


    @Override
    public Result<Void> toggleBan(Long id, boolean attemptBan) {
        if(id == null)return Result.error("评论ID不能为空");
        // 同一评论的封禁/解封串行化（幂等/防重，#24）
        return distributedLockUtil.executeWithLock("admin:comment:ban:" + id, LOCK_WAIT_SECONDS, LOCK_LEASE_SECONDS, () -> {
            Comment comment = commentRepository.findById(id).orElseThrow(NoSuchCommentException::new);
            if(attemptBan)comment.ban();
            else comment.unban();
            commentRepository.update(comment);
            return Result.success();
        });
    }

    @Override
    public Result<Void> ignoreReport(Long id) {
        if(id == null)return Result.error("评论ID不能为空");
        // 同一评论的忽略举报串行化（幂等/防重，#24）
        return distributedLockUtil.executeWithLock("admin:comment:ignoreReport:" + id, LOCK_WAIT_SECONDS, LOCK_LEASE_SECONDS, () -> {
            Comment comment = commentRepository.findById(id).orElseThrow(NoSuchCommentException::new);
            comment.ignoreReport();
            commentRepository.update(comment);
            return Result.success();
        });
    }


    private AdminCommentVO toVO(Comment comment) {
        return AdminCommentVO.builder()
                .id(comment.getId())
                .rootId(comment.getRootId())
                .publisherId(comment.getPublisherId())
                .parentId(comment.getParentId())
                .postId(comment.getPostId())
                .postStatus(comment.getPostStatus())
                .type(comment.getType())
                .content(comment.getContent())
                .createTime(comment.getCreateTime())
                .replyCount(comment.getReplyCount())
                .isTop(comment.getIsTop())
                // 状态为空时返回 null 而非 NPE（#20）
                .status(comment.getStatus() != null ? comment.getStatus().getCode() : null)
                .reportReason(comment.getReportReason())
                .item(comment.getItem())
                .extra(comment.getExtra())
                .ipLocation(comment.getIpLocation())
                .clientType(comment.getClientType())
                .likeCount(comment.getLikeCount())
                .updateTime(comment.getUpdateTime())
                .build();
    }

}
