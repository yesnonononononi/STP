package com.summit.stp.admin.application.service;

import com.summit.stp.admin.application.command.AdminPostQueryCommand;
import com.summit.stp.admin.application.vo.AdminPostVO;
import com.summit.stp.common.application.api.result.PageResult;
import com.summit.stp.common.application.api.result.Result;
import com.summit.stp.post.application.vo.PostVO;

import java.util.List;

public interface AdminPostService {
    Result<PageResult<List<AdminPostVO>>> list(AdminPostQueryCommand command);

    void bypass(Long id);

    void bypassNot(Long id, String reason);

    void toggleBan(Long id, boolean attemptBan);

    PostVO getPostById(Long id, Integer status);
}
