package com.summit.stp.user.application.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.summit.stp.common.application.domain.event.UserChangedEvent;
import com.summit.stp.common.application.domain.exception.ParameterException;
import com.summit.stp.common.application.service.queue.QueueSender;
import com.summit.stp.common.constants.MqConstants;
import com.summit.stp.user.application.command.CreateUserFollowCommand;
import com.summit.stp.user.application.service.UserFollowAppService;
import com.summit.stp.user.application.vo.UserFollowVO;
import com.summit.stp.user.domain.model.UserFollow;

import com.summit.stp.user.domain.model.UserFollowRepository;
import com.summit.stp.user.infrastructure.persistence.mapper.UserStatMapper;
import com.summit.stp.user.infrastructure.persistence.po.UserStatPO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserFollowAppServiceImpl implements UserFollowAppService {
    private final UserFollowRepository<UserFollow> userFollowRepository;
    private final UserStatMapper userStatMapper;
    private final QueueSender queueSender;

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

        UserFollow existing = userFollowRepository.findByFollowerAndFollowee(followerId, followeeId).orElse(null);
        if (existing != null) {
            // 已有记录，按业务取反状态或软删除处理
        } else {
            UserFollow follow = UserFollow.builder()
                    .followerId(followerId)
                    .followeeId(followeeId)
                    .build();
            userFollowRepository.save(follow);
            syncUpdateFansAndPublishEvent(followeeId, 1);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unfollow(Long id) {
        UserFollow existing = getExistingOrThrow(id);
        userFollowRepository.delete(existing.getId());
        syncUpdateFansAndPublishEvent(existing.getFolloweeId(), -1);
    }

    @Override
    public Page<UserFollowVO> getFolloweesPage(Long followerId, long page, long pageSize) {
        Page<UserFollow> modelPage = userFollowRepository.queryFolloweesPage(followerId, page, pageSize);
        Page<UserFollowVO> voPage = new Page<>(modelPage.getCurrent(), modelPage.getSize(), modelPage.getTotal());
        List<UserFollowVO> voList = modelPage.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);
        return voPage;
    }

    private UserFollow getExistingOrThrow(Long id) {
        return userFollowRepository.findById(id)
                .orElseThrow(() -> new ParameterException("关注关系不存在"));
    }

    /**
     * 同步更新数据库 user_stat 表的粉丝数量，并发布统一的 UserChangedEvent 消息
     */
    private void syncUpdateFansAndPublishEvent(Long userId, Integer delta) {
        if (userId == null || delta == null || delta == 0) {
            return;
        }
        int rows = userStatMapper.incrFans(userId, delta);
        if (rows == 0) {
            try {
                userStatMapper.insert(UserStatPO.builder()
                        .userId(userId)
                        .fans(Math.max(0L, delta.longValue()))
                        .topic(0L)
                        .liked(0L)
                        .build());
            } catch (DuplicateKeyException e) {
                userStatMapper.incrFans(userId, delta);
            }
        }
        log.info("【用户关系】标识：DB 动作：同步更新用户粉丝数成功, userId={}, delta={}", userId, delta);

        try {
            double scoreDelta = delta * 5.0; // 粉丝积分权重
            UserChangedEvent event = UserChangedEvent.builder()
                    .eventType(UserChangedEvent.EventType.UPDATE)
                    .userId(userId)
                    .scoreDelta(scoreDelta)
                    .build();
            queueSender.send(MqConstants.User.EXCHANGE, MqConstants.User.ROUTING_KEY_CHANGE, event);
            log.info("【用户关系】标识：MQ 动作：发送用户信息变更事件成功, userId={}, scoreDelta={}", userId, scoreDelta);
        } catch (Exception e) {
            log.error("【用户关系】标识：MQ 动作：发送用户信息变更事件失败, userId={}", userId, e);
        }
    }

    private UserFollowVO toVO(UserFollow model) {
        if (model == null) return null;
        return UserFollowVO.builder()
                .id(model.getId())
                .followerId(model.getFollowerId())
                .followeeId(model.getFolloweeId())
                .status(1)
                .createTime(model.getCreateTime())
                .build();
    }
}
