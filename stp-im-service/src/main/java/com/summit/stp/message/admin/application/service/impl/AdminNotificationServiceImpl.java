package com.summit.stp.message.admin.application.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.summit.stp.message.admin.application.command.AdminCreateNotificationCommand;
import com.summit.stp.message.admin.application.command.AdminNotificationQueryCommand;
import com.summit.stp.message.admin.application.service.AdminNotificationService;
import com.summit.stp.message.admin.application.vo.AdminNotificationVO;
import com.summit.stp.common.application.api.result.PageResult;
import com.summit.stp.common.application.api.result.Result;
import com.summit.stp.common.application.domain.exception.BusinessException;
import com.summit.stp.common.auth.UserHolder;
import com.summit.stp.common.util.DistributedLockUtil;
import com.summit.stp.message.message.domain.exception.MessageNoExistException;
import com.summit.stp.message.message.domain.model.SystemMessage;
import com.summit.stp.message.message.domain.repository.SystemMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminNotificationServiceImpl implements AdminNotificationService {

    /** 管理端写操作分布式锁等待时间（秒） */
    private static final long LOCK_WAIT_SECONDS = 3L;
    /** 管理端写操作分布式锁租约时间（秒） */
    private static final long LOCK_LEASE_SECONDS = 10L;

    private final SystemMessageRepository systemMessageRepository;
    private final DistributedLockUtil distributedLockUtil;

    @Override
    public Result<PageResult<List<AdminNotificationVO>>> listBy(AdminNotificationQueryCommand command) {
        Page<SystemMessage> res = systemMessageRepository.findPage(command);
        return Result.success(new PageResult<>(res.getCurrent(), res.getTotal(), res.getRecords().stream().map(this::toVO).toList()));
    }


    @Override
    public Result<Void> create(AdminCreateNotificationCommand command) {
        validateCreateCommand(command);
        SystemMessage systemMessage = buildModel(command);
        // 同一目标用户+同一通知类型的创建串行化，避免重复发通知（幂等/防重，#24）
        String lockKey = "admin:notification:create:" + command.getTargetUserId() + ":" + command.getNoticeType();
        return distributedLockUtil.executeWithLock(lockKey, LOCK_WAIT_SECONDS, LOCK_LEASE_SECONDS, () -> {
            systemMessageRepository.save(systemMessage);
            return Result.success();
        });
    }

    @Override
    public Result<Void> revoke(Long id) {
        if(id == null)return Result.error("通知ID不能为空");
        // 同一通知的撤销串行化（幂等/防重，#24）
        return distributedLockUtil.executeWithLock("admin:notification:revoke:" + id, LOCK_WAIT_SECONDS, LOCK_LEASE_SECONDS, () -> {
            SystemMessage systemMessage = systemMessageRepository.findById(id).orElseThrow(MessageNoExistException::new);
            systemMessage.revoke();
            systemMessageRepository.updateById(systemMessage);
            return Result.success();
        });
    }

    @Override
    public Result<Void> delete(Long id) {
        if(id == null)return Result.error("通知ID不能为空");
        // 同一通知的删除串行化（幂等/防重，#24）
        return distributedLockUtil.executeWithLock("admin:notification:delete:" + id, LOCK_WAIT_SECONDS, LOCK_LEASE_SECONDS, () -> {
            SystemMessage systemMessage = systemMessageRepository.findById(id).orElseThrow(MessageNoExistException::new);
            systemMessage.delete();
            systemMessageRepository.updateById(systemMessage);
            return Result.success();
        });
    }

    @Override
    public Result<Void> publish(Long id) {
        if(id == null)return Result.error("通知ID不能为空");
        // 同一通知的发布串行化，避免重复发布（幂等/防重，#24）
        return distributedLockUtil.executeWithLock("admin:notification:publish:" + id, LOCK_WAIT_SECONDS, LOCK_LEASE_SECONDS, () -> {
            SystemMessage systemMessage = systemMessageRepository.findById(id).orElseThrow(MessageNoExistException::new);
            systemMessage.publish();
            systemMessageRepository.updateById(systemMessage);
            return Result.success();
        });
    }


    private AdminNotificationVO toVO(SystemMessage systemMessage) {
        Instant publicTime = systemMessage.getPublicTime();
        Instant createTime = systemMessage.getCreateTime();
        Instant updateTime = systemMessage.getUpdateTime();
        return AdminNotificationVO.builder()
                .id(systemMessage.getId())
                .fromUserId(systemMessage.getFromUserId())
                .images(systemMessage.getImages())
                .content(systemMessage.getContent())
                .status(systemMessage.getStatus().getCode())
                .associateUser(systemMessage.getAssociateUser())
                .type(systemMessage.getType().getCode())
                .publicTime(publicTime == null ? null :Timestamp.from(publicTime))
                .createTime(createTime == null ? null :Timestamp.from(createTime))
                .updateTime(updateTime == null ? null :Timestamp.from(updateTime))
                .build();
    }


    private SystemMessage buildModel(AdminCreateNotificationCommand command) {
        return SystemMessage.builder()
                .fromUserId(UserHolder.getUser().getId())
                .images(command.getImageUrls())
                .content(command.getContent())
                .status(SystemMessage.SystemMessageStatus.DRAFT)
                .associateUser(command.getTargetUserId())
                .type(SystemMessage.SystemMessageType.fromCode(command.getNoticeType()))
                .publicTime(null)
                .createTime(Instant.now())
                .updateTime(Instant.now())
                .build();
    }

    private void validateCreateCommand(AdminCreateNotificationCommand command) {
        Integer noticeType = command.getNoticeType();
        String title = command.getTitle();
        Integer targetType = command.getTargetType();
        Long targetUserId = command.getTargetUserId();
        String content = command.getContent();
        List<String> imageUrls = command.getImageUrls();
        if(imageUrls == null)command.setImageUrls(List.of());
        if (noticeType == null) throw new BusinessException("通知类型不能为空");
        if (title == null) throw new BusinessException("标题不能为空");
        if (targetType == null) throw new BusinessException("目标类型不能为空");
        if (targetUserId == null && targetType.equals(SystemMessage.SystemMessageType.PRIVATE.getCode()))
            throw new BusinessException("目标用户不能为空");
        if (content == null) throw new BusinessException("内容不能为空");
    }

}
