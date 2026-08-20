package com.summit.stp.message.admin.application.service;

import com.summit.stp.message.admin.application.command.AdminCreateNotificationCommand;
import com.summit.stp.message.admin.application.command.AdminNotificationQueryCommand;
import com.summit.stp.message.admin.application.vo.AdminNotificationVO;
import com.summit.stp.common.application.api.result.PageResult;
import com.summit.stp.common.application.api.result.Result;

import java.util.List;

public interface AdminNotificationService {
    Result<PageResult<List<AdminNotificationVO>>> listBy(AdminNotificationQueryCommand command);

    Result<Void> create(AdminCreateNotificationCommand command);

    Result<Void> revoke(Long id);

    Result<Void> delete(Long id);

    Result<Void> publish(Long id);
}
