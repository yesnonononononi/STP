package com.summit.stp.admin.application.service;

import com.summit.stp.admin.application.command.CreateCouponActivityCommand;
import com.summit.stp.admin.application.command.CreateCouponCommand;
import com.summit.stp.admin.application.vo.AdminCouponActivityVO;
import com.summit.stp.admin.application.vo.AdminCouponVO;
import com.summit.stp.common.application.api.result.PageResult;
import com.summit.stp.common.application.api.result.Result;

import java.util.List;

public interface AdminCouponItemsService {
    Result<PageResult<List<AdminCouponVO>>> list(String keyword, Integer status, Integer page, Integer pageSize);

    Result<Void> create(CreateCouponCommand command);

    Result<Void> toggleBan(Long id, boolean attemptBan);

    Result<Void> delete(Long id);

    Result<PageResult<List<AdminCouponActivityVO>>> listActivity(String keyword, Integer status, Integer page, Integer pageSize);

    void createActivity(CreateCouponActivityCommand command);

    void toggleActivityStatus(Long id, boolean attemptStart);
}
