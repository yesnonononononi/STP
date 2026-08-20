package com.summit.stp.comment.admin.application.service;

import com.summit.stp.comment.admin.application.command.CommentQueryCommand;
import com.summit.stp.comment.admin.application.vo.AdminCommentVO;
import com.summit.stp.common.application.api.result.PageResult;
import com.summit.stp.common.application.api.result.Result;

import java.util.List;

public interface AdminCommentService {

    Result<PageResult<List<AdminCommentVO>>> listBy(CommentQueryCommand command);

    Result<Void> toggleBan(Long id, boolean attemptBan);

    Result<Void> ignoreReport(Long id);
}
