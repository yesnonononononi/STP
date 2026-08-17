package com.summit.stp.admin.application.service;

import com.summit.stp.admin.application.command.AddAdminCommand;
import com.summit.stp.admin.application.vo.AdminVO;
import com.summit.stp.admin.domain.model.Admin;
import com.summit.stp.common.application.api.result.Result;

import java.util.List;

public interface AdminService {

    Result<List<AdminVO>> list(Integer page, Integer pageSize);

    Result<Void> toggleBan(Long aId, boolean attemptBan);

    Result<Void> changeOrder(Long aId, boolean attemptAscend);

    Integer is(Long uid);

    Result<Void> add(AddAdminCommand command);

    Admin checkAdmin(Long aId);
}
