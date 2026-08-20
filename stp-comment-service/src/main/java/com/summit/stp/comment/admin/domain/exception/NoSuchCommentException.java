package com.summit.stp.comment.admin.domain.exception;

import com.summit.stp.common.application.domain.exception.BusinessException;

public class NoSuchCommentException extends BusinessException {
    public NoSuchCommentException() {
        super("评论不存在");
    }
}
