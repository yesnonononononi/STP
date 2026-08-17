package com.summit.stp.admin.application.service.impl;

import com.summit.stp.admin.application.command.AddAdminCommand;
import com.summit.stp.admin.application.service.AdminEventSender;
import com.summit.stp.admin.application.service.AdminService;
import com.summit.stp.admin.application.vo.AdminVO;
import com.summit.stp.admin.domain.model.Admin;
import com.summit.stp.admin.infrastructure.persistence.repo.AdminRepositoryImpl;
import com.summit.stp.common.application.domain.event.AdminChangedEvent;
import com.summit.stp.common.application.domain.exception.BusinessException;
import com.summit.stp.common.application.domain.model.UserSession;
import com.summit.stp.common.auth.UserHolder;
import com.summit.stp.common.application.api.result.Result;
import com.summit.stp.user.domain.model.User;
import com.summit.stp.common.application.service.queue.QueueSender;
import com.summit.stp.common.constants.MqConstants;
import com.summit.stp.user.infrastructure.persistence.UserRepositoryImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminServiceImpl implements AdminService {
    private final AdminRepositoryImpl adminRepositoryImpl;
    private final UserRepositoryImpl userRepositoryImpl;
    private final AdminEventSender adminEventSender;
    private final QueueSender queueSender;

    @Override
    public Result<List<AdminVO>> list(Integer page, Integer pageSize) {
        List<Admin> list = adminRepositoryImpl.list(page, pageSize);
        return Result.success(list.stream().map(AdminVO::toVO).toList());
    }

    @Override
    public Result<Void> toggleBan(Long aId, boolean attemptBan) {
        Admin admin = checkAdmin(aId);
        //3,禁用或者解除禁用
        if (attemptBan) {
            admin.ban();
        } else {
            admin.unban();
        }
        //4,保存
        adminRepositoryImpl.updateById(admin);

        return Result.success();
    }

    @Override
    public Result<Void> changeOrder(Long aId, boolean attemptAscend) {
        Admin admin = checkAdmin(aId);
        if (attemptAscend) {
            admin.ascend(1);
        } else {
            admin.descend(1);
        }
        adminRepositoryImpl.updateById(admin);
        return Result.success();
    }

    @Override
    public Integer is(Long uid) {
        Admin admin = adminRepositoryImpl.findByUserId(uid);
        if (admin == null) return null;
        return admin.getOrder();
    }

    @Override
    public Result<Void> add(AddAdminCommand command) {
        Integer order = command.getOrder();
        Long uid = command.getUid();
        UserSession user = UserHolder.getUser();
        if (uid == null) return Result.error("用户ID不能为空");

        //1, 查询用户信息
        Admin curAdmin = adminRepositoryImpl.findByUserId(user.getId());
        User target = userRepositoryImpl.findUserById(uid).orElseThrow(() -> new BusinessException("用户不存在"));

        //2, 检查用户信息
        //2.1 预构建Admin实体
        Admin targetAdmin = Admin.builder().userId(uid).username(target.getUsername().getValue()).order(Objects.requireNonNullElse(order, 0)).createTime(Instant.now()).updateTime(Instant.now()).build();
        //2.2 当前用户的order > target-order -> access
        if (!curAdmin.can(targetAdmin)) return Result.error("无权限操作");

        //3, 保存
        adminRepositoryImpl.save(targetAdmin);

        //4, 发布管理员新增事件
        adminEventSender.send(AdminChangedEvent.builder().uid(uid).type("insert").build());

        return Result.success();

    }


    /**
     * 校验管理员权限
     * @param aId 被操作的管理员ID
     * @return 被操作的管理员实体
     */
    @Override
    public Admin checkAdmin(Long aId) {
        Long uid = UserHolder.getUser().getId();
        //1, 获取目前用户和被封禁的管理员信息
        Admin me = adminRepositoryImpl.findByUserId(uid);
        if (me == null) {
            throw new BusinessException("当前用户无管理员权限");
        }
        Admin admin = adminRepositoryImpl.findById(aId).orElse(null);
        if (admin == null) {
            throw new BusinessException("目标管理员不存在");
        }

        //2,身份不能低于被封禁的管理员
        if (!me.can(admin)) throw new BusinessException("无权限操作");
        return admin;
    }



}
