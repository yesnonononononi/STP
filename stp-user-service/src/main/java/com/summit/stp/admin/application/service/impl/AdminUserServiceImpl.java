package com.summit.stp.admin.application.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.summit.stp.admin.application.command.AdminUserQueryCommand;
import com.summit.stp.admin.application.service.AdminService;
import com.summit.stp.common.application.api.result.PageResult;
import com.summit.stp.common.application.domain.exception.NoFoundUserInfoException;
import com.summit.stp.elasticsearch.service.AdminUserQuerySupport;
import com.summit.stp.admin.application.service.AdminUserService;
import com.summit.stp.common.application.api.result.Result;
import com.summit.stp.common.auth.UserHolder;
import com.summit.stp.common.application.api.result.ESPageVO;
import com.summit.stp.common.util.DistributedLockUtil;
import com.summit.stp.user.api.vo.UserProfileVO;
import com.summit.stp.user.application.service.UserApplicationService;
import com.summit.stp.user.domain.model.User;
import com.summit.stp.user.domain.repository.UserRepository;
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

    /** 管理端写操作分布式锁等待时间（秒） */
    private static final long LOCK_WAIT_SECONDS = 3L;
    /** 管理端写操作分布式锁租约时间（秒） */
    private static final long LOCK_LEASE_SECONDS = 10L;

    private final AdminUserQuerySupport adminUserQuerySupport;
    private final UserApplicationService userAPPService;
    private final AdminService adminService;
    private final UserRepository<User> userRepository;
    private final QueueSender queueSender;
    private final DistributedLockUtil distributedLockUtil;

    @Override
    public Result<PageResult<List<UserProfileVO>>> listBy(AdminUserQueryCommand command) {
        if (!command.isQueryWithoutFilter()) {
            return esQuery(command);
        }
        Page<UserProfileVO> page = userAPPService.findPage(command.getPage(), command.getSize());
        // 管理端响应统一脱敏 PII（#21）；仅作用于本次管理端查询结果对象，不影响 C 端链路
        page.getRecords().forEach(UserProfileVO::maskSensitiveInfo);
        return Result.success(new PageResult<>(page.getCurrent(), page.getTotal(), page.getRecords()));
    }


    @Override
    public Result<Void> toggleBan(Long uid, boolean attemptBan) {
        if (uid == null) return Result.error("用户ID不能为空");
        Integer curAdmin = UserHolder.getUser().getAdmin();
        // 当前操作者无管理员等级信息时直接拒绝，避免后续比较 NPE（#20）
        if (curAdmin == null) return Result.error("当前用户无管理员权限");
        // 同一用户的封禁/解封串行化（幂等/防重，#24）
        return distributedLockUtil.executeWithLock("admin:user:ban:" + uid, LOCK_WAIT_SECONDS, LOCK_LEASE_SECONDS, () -> {
            Integer target = isAdmin(uid);
            if (target != null && curAdmin <= target) {
                return Result.error("无权限操作");
            }
            User user = userRepository.findUserById(uid).orElseThrow(NoFoundUserInfoException::new);
            user.toggleBan(attemptBan);
            userRepository.updateById(user);

            // 发布用户 ES 专属更新事件，驱动 ES 实时拉取最新用户状态同步
            queueSender.sendUpdateEvent(MqConstants.Es.EXCHANGE, MqConstants.Es.ROUTING_KEY_USER_UPDATE, user.getId());

            return Result.success();
        });
    }

    @Override
    public Result<Long> countNewUser() {
        return Result.success(userRepository.countNewUser());
    }


    public Integer isAdmin(Long uid) {
        return adminService.is(uid);
    }


    /**
     * if acquire filter data from es
     * @param command 查询条件
     */
    private Result<PageResult<List<UserProfileVO>>>  esQuery(AdminUserQueryCommand command){
        ESPageVO<Long> res = adminUserQuerySupport.listBy(command);
        List<Long> ids = res.getData();
        // 无匹配结果时返回空页，保持 Result 契约（历史缺陷 #20：此前 return null 破坏契约）
        if (ids == null || ids.isEmpty()) return Result.success(PageResult.empty());
        List<UserProfileVO> list = userAPPService.findProfileByIds(ids).stream()
                .sorted(Comparator.comparing(UserProfileVO::getId))
                .peek(UserProfileVO::maskSensitiveInfo)
                .toList();
        if (list.isEmpty()) return Result.success(PageResult.empty());
        return Result.success(new PageResult<>(res.getPage(), res.getTotal(), list));
    }
}
