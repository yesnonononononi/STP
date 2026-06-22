package com.summit.stp.user.application.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.summit.stp.shared.exception.ParameterException;
import com.summit.stp.user.application.command.CreateUserFollowCommand;
import com.summit.stp.user.application.command.UpdateUserFollowCommand;
import com.summit.stp.user.application.vo.UserFollowVO;
import com.summit.stp.user.domain.event.UserFansChangeEvent;
import com.summit.stp.user.domain.repository.UserFollowRepository;
import com.summit.stp.user.infrastructure.persistence.po.UserFollowPO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserFollowAppServiceImpl implements UserFollowAppService {
    private final UserFollowRepository userFollowRepository;
    private final UserMessageSender userMessageSender;

    @Override
    public UserFollowVO getUserFollowById(Long id) {
        UserFollowPO po = userFollowRepository.findById(id);
        return toVO(po);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void follow(CreateUserFollowCommand command) {
        Long followerId = command.getFollowerId();
        Long followeeId = command.getFolloweeId();
        if (followerId == null || followeeId == null) {
            throw new ParameterException("用户ID不能为空");
        }
        if (followerId.equals(followeeId)) {
            throw new ParameterException("用户不能关注自己");
        }

        UserFollowPO existing = userFollowRepository.findByFollowerAndFollowee(followerId, followeeId);
        int delta = 0;
        if (existing != null) {
            if (existing.getStatus() == 1) {
                // 已关注 -> 取消关注 (Toggle off)
                existing.setStatus(2);
                delta = -1;
            } else {
                // 已取消 -> 重新关注 (Toggle on)
                existing.setStatus(1);
                existing.setSource(command.getSource());
                delta = 1;
            }
            userFollowRepository.save(existing);
        } else {
            // 首次关注 (Toggle on)
            UserFollowPO follow = new UserFollowPO();
            follow.setFollowerId(followerId);
            follow.setFolloweeId(followeeId);
            follow.setStatus(1); // 1-正常关注
            follow.setSource(command.getSource());
            userFollowRepository.save(follow);
            delta = 1;
        }

        if (delta != 0) {
            sendFansChangeEvent(followeeId, delta);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateFollow(UpdateUserFollowCommand command) {
        UserFollowPO existing = userFollowRepository.findById(command.getId());
        if (existing == null) {
            throw new ParameterException("关注关系不存在");
        }

        Integer oldStatus = existing.getStatus();
        Integer newStatus = command.getStatus();
        if (oldStatus.equals(newStatus)) {
            existing.setSource(command.getSource());
            userFollowRepository.save(existing);
            return;
        }

        existing.setStatus(newStatus);
        existing.setSource(command.getSource());
        userFollowRepository.save(existing);

        // 状态变更，处理 fans 的 MQ 消息
        if (oldStatus == 1 && newStatus == 2) {
            // 取消关注：被关注者粉丝 -1
            sendFansChangeEvent(existing.getFolloweeId(), -1);
        } else if (oldStatus == 2 && newStatus == 1) {
            // 恢复关注：被关注者粉丝 +1
            sendFansChangeEvent(existing.getFolloweeId(), 1);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unfollow(Long id) {
        UserFollowPO existing = userFollowRepository.findById(id);
        if (existing == null) {
            throw new ParameterException("关注关系不存在");
        }

        if (existing.getStatus() == 2) {
            // 已经取消关注，无需操作
            return;
        }

        existing.setStatus(2); // 2-已取消
        userFollowRepository.save(existing);

        // 粉丝 -1
        sendFansChangeEvent(existing.getFolloweeId(), -1);
    }

    @Override
    public Page<UserFollowVO> getFollowersPage(Long followeeId, long page, long pageSize) {
        Page<UserFollowPO> poPage = userFollowRepository.queryFollowersPage(followeeId, page, pageSize);
        Page<UserFollowVO> voPage = new Page<>(poPage.getCurrent(), poPage.getSize(), poPage.getTotal());
        List<UserFollowVO> voList = poPage.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public Page<UserFollowVO> getFolloweesPage(Long followerId, long page, long pageSize) {
        Page<UserFollowPO> poPage = userFollowRepository.queryFolloweesPage(followerId, page, pageSize);
        Page<UserFollowVO> voPage = new Page<>(poPage.getCurrent(), poPage.getSize(), poPage.getTotal());
        List<UserFollowVO> voList = poPage.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);
        return voPage;
    }

    private void sendFansChangeEvent(Long userId, Integer delta) {
        UserFansChangeEvent event = UserFansChangeEvent.builder()
                .userId(userId)
                .fansDelta(delta)
                .build();
        try {
            userMessageSender.sendFansUpdate(event);
            log.info("【用户关系模块】已发送粉丝数MQ变更通知: userId={}, delta={}", userId, delta);
        } catch (Exception e) {
            log.error("【用户关系模块】发送粉丝数MQ变更通知失败: userId={}, delta={}", userId, delta, e);
        }
    }

    private UserFollowVO toVO(UserFollowPO po) {
        if (po == null) return null;
        return UserFollowVO.builder()
                .id(po.getId())
                .followerId(po.getFollowerId())
                .followeeId(po.getFolloweeId())
                .status(po.getStatus())
                .source(po.getSource())
                .createTime(po.getCreateTime())
                .updateTime(po.getUpdateTime())
                .build();
    }
}
