package com.summit.stp.message.admin.application.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.summit.stp.message.admin.application.command.AdminCreateNotificationCommand;
import com.summit.stp.message.admin.application.command.AdminNotificationQueryCommand;
import com.summit.stp.message.admin.application.service.AdminNotificationService;
import com.summit.stp.message.admin.application.vo.AdminNotificationVO;
import com.summit.stp.common.annotation.Admin;
import com.summit.stp.common.annotation.Login;
import com.summit.stp.common.application.api.result.PageResult;
import com.summit.stp.common.application.api.result.Result;
import com.summit.stp.common.application.domain.exception.BusinessException;
import com.summit.stp.common.auth.UserHolder;
import com.summit.stp.message.message.domain.exception.MessageNoExistException;
import com.summit.stp.message.message.domain.model.SystemMessage;
import com.summit.stp.message.message.domain.repository.SystemMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Service
@Slf4j
@Admin
@Login
@RequiredArgsConstructor
public class AdminNotificationServiceImpl implements AdminNotificationService {
    private final SystemMessageRepository systemMessageRepository;

    @Override
    public Result<PageResult<List<AdminNotificationVO>>> listBy(@RequestBody AdminNotificationQueryCommand command) {
        Page<SystemMessage> res = systemMessageRepository.findPage(command);
        return Result.success(new PageResult<>(res.getCurrent(), res.getTotal(), res.getRecords().stream().map(this::toVO).toList()));
    }


    @Override
    public Result<Void> create(@RequestBody AdminCreateNotificationCommand command) {
        validateCreateCommand(command);
        SystemMessage systemMessage = buildModel(command);
        systemMessageRepository.save(systemMessage);
        return Result.success();
    }

    @Override
    public Result<Void> revoke(Long id) {
        if(id == null)return Result.error("通知ID不能为空");
        SystemMessage systemMessage = systemMessageRepository.findById(id).orElseThrow(MessageNoExistException::new);
        systemMessage.revoke();
        systemMessageRepository.updateById(systemMessage);
        return Result.success();
    }

    @Override
    public Result<Void> delete(Long id) {
        if(id == null)return Result.error("通知ID不能为空");
        SystemMessage systemMessage = systemMessageRepository.findById(id).orElseThrow(MessageNoExistException::new);
        systemMessage.delete();
        systemMessageRepository.updateById(systemMessage);
        return Result.success();
    }

    @Override
    public Result<Void> publish(Long id) {
        if(id == null)return Result.error("通知ID不能为空");
        SystemMessage systemMessage = systemMessageRepository.findById(id).orElseThrow(MessageNoExistException::new);
        systemMessage.publish();
        systemMessageRepository.updateById(systemMessage);
        return Result.success();
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
