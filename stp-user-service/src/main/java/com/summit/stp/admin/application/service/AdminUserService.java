package com.summit.stp.admin.application.service;

import com.summit.stp.admin.application.command.AdminUserQueryCommand;
import com.summit.stp.common.application.api.result.PageResult;
import com.summit.stp.common.application.api.result.Result;
import com.summit.stp.user.api.vo.UserProfileVO;

import java.util.List;

public interface AdminUserService {
    Result<PageResult<List<UserProfileVO>>> listBy(AdminUserQueryCommand command);

    Result<Void> toggleBan(Long uid, boolean attemptBan);

    Result<Long> countNewUser();

}
