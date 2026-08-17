package com.summit.stp.elasticsearch.service;

import com.summit.stp.admin.application.command.AdminUserQueryCommand;
import com.summit.stp.common.application.api.result.ESPageVO;

public interface AdminUserQuerySupport {
    ESPageVO<Long> listBy(AdminUserQueryCommand command);
}
