package com.summit.stp.user.application.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.summit.stp.common.application.domain.event.UserChangedEvent;
import com.summit.stp.common.application.domain.exception.BusinessException;
import com.summit.stp.common.application.domain.exception.ParameterException;
import com.summit.stp.common.application.service.queue.QueueSender;
import com.summit.stp.common.constants.MqConstants;
import com.summit.stp.user.application.command.CreateUserFollowCommand;
import com.summit.stp.user.application.service.UserFollowAppService;
import com.summit.stp.user.application.vo.UserFollowVO;
import com.summit.stp.user.domain.model.User;
import com.summit.stp.user.domain.model.UserFollow;


import com.summit.stp.user.domain.repository.UserFollowRepository;
import com.summit.stp.user.domain.repository.UserRepository;
import com.summit.stp.user.infrastructure.persistence.mapper.UserStatMapper;
import com.summit.stp.user.infrastructure.persistence.po.UserStatPO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserFollowAppServiceImpl implements UserFollowAppService {
    private final UserFollowRepository<UserFollow> userFollowRepository;
    private final UserStatMapper userStatMapper;
    private final QueueSender queueSender;
    private final UserRepository<User> userRepository;

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

        // 查看对方是否关注自己
        UserFollow existingReverse = userFollowRepository.findByFollowerAndFollowee(followeeId, followerId).orElse(null);
        boolean isMutualFollow = existingReverse != null && existingReverse.isActive();

        // 检查自己是否已经关注过对方
        UserFollow existing = userFollowRepository.findByFollowerAndFollowee(followerId, followeeId).orElse(null);
        if (existing != null && existing.isActive()) throw new BusinessException("用户已关注");
        if (existing == null) {
            // 保存自己 follow target 的关注关系
            existing = UserFollow.builder()
                    .source(command.getSource())
                    .followerId(followerId)
                    .followeeId(followeeId)
                    .status(!isMutualFollow ? UserFollow.FollowStatus.NORMAL : UserFollow.FollowStatus.EACH)  // 如果对方没有关注自己，则状态为 NORMAL，否则为 EACH
                    .build();
            userFollowRepository.save(existing);
        } else {
            if (isMutualFollow) existing.eachFollow();  //如果对方关注着自己,那么自己的状态要改为回关
            else existing.follow();
            userFollowRepository.updateById(existing);
        }
        // 对方关注自己了, 自己回关,对方的状态也要改为回关
        if(existingReverse!=null && existingReverse.isActive()) {
            existingReverse.eachFollow();
            userFollowRepository.updateById(existingReverse);
        }
        syncUpdateFansAndPublishEvent(followeeId, 1);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unfollow(Long id) {
        UserFollow existing = getExistingOrThrow(id);
        Long followeeId = existing.getFolloweeId();
        UserFollow targetFollow = userFollowRepository.findByFollowerAndFollowee(existing.getFolloweeId(), existing.getFollowerId()).orElse(null);
        if (targetFollow != null && targetFollow.isEachFollow()) {
            targetFollow.follow();
            userFollowRepository.updateById(targetFollow);
        }
        existing.cancel();
        userFollowRepository.updateById(existing);
        syncUpdateFansAndPublishEvent(followeeId, -1);
    }

    @Override
    public Page<UserFollowVO> getFolloweesPage(Long followeeId, long page, long pageSize) {
        Page<UserFollow> modelPage = userFollowRepository.queryFolloweesPage(followeeId, page, pageSize);
        Page<UserFollowVO> voPage = new Page<>(modelPage.getCurrent(), modelPage.getSize(), modelPage.getTotal());
        List<UserFollow> records = modelPage.getRecords();
        if (records.isEmpty()) {
            voPage.setRecords(Collections.emptyList());
            return voPage;
        }

        // 批量查询被关注用户信息，装配昵称与头像
        List<Long> followerIds = records.stream()
                .map(UserFollow::getFollowerId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, User> userMap = userRepository.findUserByIds(followerIds);

        List<UserFollowVO> voList = records.stream()
                .map(model -> {
                    UserFollowVO vo = toVO(model);
                    User follower = userMap.get(model.getFollowerId());
                    if (follower != null) {
                        vo.setNick(resolveNick(follower));
                        vo.setAvatar(follower.getAvatar());
                    }
                    return vo;
                })
                .collect(Collectors.toList());
        voPage.setRecords(voList);
        return voPage;
    }

    /**
     * 解析用户展示昵称：优先 nick，其次用户名
     */
    private String resolveNick(User user) {
        if (user.getNick() != null && !user.getNick().isEmpty()) {
            return user.getNick();
        }
        return user.getUsername() != null ? user.getUsername().getValue() : null;
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
                .status(model.getStatus().getCode())
                .createTime(model.getCreateTime())
                .build();
    }
}