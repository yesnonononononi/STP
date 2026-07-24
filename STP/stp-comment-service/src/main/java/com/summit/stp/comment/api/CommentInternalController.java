package com.summit.stp.comment.api;

import com.summit.stp.shared.application.vo.CommentSimpleVO;
import com.summit.stp.shared.result.Result;
import com.summit.stp.comment.domain.model.Comment;
import com.summit.stp.comment.domain.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 评论内部接口 - 供其他微服务通过 Feign 调用
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/post/comment/internal")
public class CommentInternalController {

    private final CommentRepository commentRepository;

    /**
     * 查询评论简要信息（供 IM 服务获取评论元数据用于通知）
     */
    @GetMapping("/simple/{id}")
    public Result<CommentSimpleVO> findSimpleCommentById(@PathVariable Long id) {
        Comment comment = commentRepository.findById(id);
        if (comment == null) {
            return Result.success(null);
        }
        return Result.success(CommentSimpleVO.builder()
                .id(comment.getId())
                .postId(comment.getPostId())
                .parentId(comment.getParentId())
                .publisherId(comment.getPublisherId())
                .content(comment.getContent())
                .type(comment.getType() != null ? comment.getType().getCode() : null)
                .build());
    }
}
