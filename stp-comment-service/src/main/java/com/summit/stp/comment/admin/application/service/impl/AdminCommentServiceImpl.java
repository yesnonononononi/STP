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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminCommentServiceImpl implements AdminCommentService {
    private final CommentRepository commentRepository;

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
        Comment comment = commentRepository.findById(id).orElseThrow(NoSuchCommentException::new);
        if(attemptBan)comment.ban();
        else comment.unban();
        commentRepository.update(comment);
        return Result.success();
    }

    @Override
    public Result<Void> ignoreReport(Long id) {
        if(id == null)return Result.error("评论ID不能为空");
        Comment comment = commentRepository.findById(id).orElseThrow(NoSuchCommentException::new);
        comment.ignoreReport();
        commentRepository.update(comment);
        return Result.success();
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
                .status(comment.getStatus().getCode())
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
