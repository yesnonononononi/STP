package com.summit.stp.admin.application.service.impl;

import com.summit.stp.admin.application.command.AdminUserQueryCommand;
import com.summit.stp.admin.application.service.AdminService;
import com.summit.stp.common.application.api.result.PageResult;
import com.summit.stp.common.application.domain.exception.NoFoundUserInfoException;
import com.summit.stp.elasticsearch.service.AdminUserQuerySupport;
import com.summit.stp.admin.application.service.AdminUserService;
import com.summit.stp.common.application.api.result.Result;
import com.summit.stp.common.auth.UserHolder;
import com.summit.stp.common.application.api.result.ESPageVO;
import com.summit.stp.user.api.vo.UserProfileVO;
import com.summit.stp.user.application.service.impl.UserAPPServiceImpl;
import com.summit.stp.user.domain.model.User;
import com.summit.stp.user.infrastructure.persistence.UserRepositoryImpl;
import com.summit.stp.common.application.service.queue.QueueSender;
import com.summit.stp.common.constants.MqConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {
    private final AdminUserQuerySupport adminUserQuerySupport;
    private final UserAPPServiceImpl userAPPServiceImpl;
    private final AdminService adminService;
    private final UserRepositoryImpl userRepositoryImpl;
    private final QueueSender queueSender;

    @Override
    public Result<PageResult<List<UserProfileVO>>> listBy(AdminUserQueryCommand command) {
        ESPageVO<Long> res = adminUserQuerySupport.listBy(command);
        List<Long> ids = res.getData();
        List<UserProfileVO> list = userAPPServiceImpl.findProfileByIds(ids).stream().sorted(Comparator.comparing(UserProfileVO::getId)).toList();
        if (list.isEmpty()) return Result.success(PageResult.empty());
        return Result.success(new PageResult<>(res.getPage(), res.getTotal(), list));
    }


    @Override
    public Result<Void> toggleBan(Long uid, boolean attemptBan) {
        Integer target;
        Integer curAdmin = UserHolder.getUser().getAdmin();
        User user = userRepositoryImpl.findUserById(uid).orElseThrow(NoFoundUserInfoException::new);
        if ((target = isAdmin(uid)) != null && curAdmin <= target) {
            return Result.error("无权限操作");
        }
        user.toggleBan(attemptBan);
        userRepositoryImpl.updateById(user);

        // 发布用户关键更新事件，驱动 ES 实时拉取最新用户状态同步
        queueSender.sendUpdateEvent(MqConstants.User.EXCHANGE, MqConstants.User.ROUTING_KEY_ES_UPDATE, user.getId());

        return Result.success();
    }


    public Integer isAdmin(Long uid) {
        return adminService.is(uid);
    }
}
