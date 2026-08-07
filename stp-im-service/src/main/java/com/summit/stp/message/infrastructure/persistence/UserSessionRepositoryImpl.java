package com.summit.stp.message.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.summit.stp.message.domain.model.UserSession;
import com.summit.stp.message.domain.repository.UserSessionRepository;
import com.summit.stp.message.infrastructure.persistence.mapper.UserSessionMapper;
import com.summit.stp.message.infrastructure.persistence.po.UserSessionPO;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserSessionRepositoryImpl implements UserSessionRepository {
    private final UserSessionMapper userSessionMapper;

    public UserSessionRepositoryImpl(UserSessionMapper userSessionMapper) {
        this.userSessionMapper = userSessionMapper;
    }

    public UserSessionPO toPO(UserSession userSession) {
        return UserSessionPO.builder()
                .publicId(userSession.getId())
                .userId(userSession.getUserId())
                .sessionId(userSession.getSessionId())
                .isTop(userSession.getIsTop())
                .isMute(userSession.getIsMute())
                .draft(userSession.getDraft())
                .unreadCount(userSession.getUnreadCount())
                .isHidden(userSession.getIsHidden())
                .targetNickName(userSession.getTargetNickName())
                .targetAvatar(userSession.getTargetAvatar())
                .targetId(userSession.getTargetId())
                .createTime(userSession.getCreateTime())
                .updateTime(userSession.getUpdateTime())
                .build();
    }

    public UserSession toDomain(UserSessionPO po) {
        return UserSession.builder()
                .id(po.getPublicId())
                .userId(po.getUserId())
                .sessionId(po.getSessionId())
                .isTop(po.getIsTop())
                .isMute(po.getIsMute())
                .targetId(po.getTargetId())
                .draft(po.getDraft())
                .unreadCount(po.getUnreadCount())
                .isHidden(po.getIsHidden())
                .targetNickName(po.getTargetNickName())
                .targetAvatar(po.getTargetAvatar())
                .createTime(po.getCreateTime())
                .updateTime(po.getUpdateTime())
                .build();
    }

    @Override
    public UserSession findByUserIdAndSessionId(Long userId, Long sessionId) {
        LambdaQueryWrapper<UserSessionPO> queryWrapper = new LambdaQueryWrapper<UserSessionPO>()
                .eq(UserSessionPO::getUserId, userId)
                .eq(UserSessionPO::getSessionId, sessionId);
        UserSessionPO po = userSessionMapper.selectOne(queryWrapper);
        return po == null ? null : toDomain(po);
    }

    @Override
    public UserSession findTargetSession(Long sessionId, Long userId) {
        LambdaQueryWrapper<UserSessionPO> queryWrapper = new LambdaQueryWrapper<UserSessionPO>()
                .eq(UserSessionPO::getSessionId, sessionId)
                .ne(UserSessionPO::getUserId, userId)
                .last("limit 1");
        UserSessionPO po = userSessionMapper.selectOne(queryWrapper);
        return po == null ? null : toDomain(po);
    }

    @Override
    public List<UserSession> findByUserId(Long userId) {
        LambdaQueryWrapper<UserSessionPO> queryWrapper = new LambdaQueryWrapper<UserSessionPO>()
                .eq(UserSessionPO::getUserId, userId);
        return userSessionMapper.selectList(queryWrapper).stream().map(this::toDomain).toList();
    }

    @Override
    public void save(UserSession userSession) {
        UserSessionPO po = toPO(userSession);
        UserSessionPO existing = findPOByUserIdAndSessionId(userSession.getUserId(), userSession.getSessionId());
        if (existing == null) {
            userSessionMapper.insert(po);
            return;
        }
        userSessionMapper.updateById(withInternalIdAndPublicId(po, existing.getId(), existing.getPublicId()));
    }

    @Override
    public void batchUpdate(List<UserSession> list) {
        list.forEach(this::save);
    }

    private UserSessionPO findPOByUserIdAndSessionId(Long userId, Long sessionId) {
        return userSessionMapper.selectOne(new LambdaQueryWrapper<UserSessionPO>()
                .eq(UserSessionPO::getUserId, userId)
                .eq(UserSessionPO::getSessionId, sessionId));
    }

    private UserSessionPO withInternalIdAndPublicId(UserSessionPO po, Long internalId, Long publicId) {
        return UserSessionPO.builder()
                .id(internalId)
                .publicId(publicId != null ? publicId : po.getPublicId())
                .userId(po.getUserId())
                .targetId(po.getTargetId())
                .sessionId(po.getSessionId())
                .isTop(po.getIsTop())
                .isMute(po.getIsMute())
                .draft(po.getDraft())
                .unreadCount(po.getUnreadCount())
                .isHidden(po.getIsHidden())
                .targetNickName(po.getTargetNickName())
                .targetAvatar(po.getTargetAvatar())
                .createTime(po.getCreateTime())
                .updateTime(po.getUpdateTime())
                .build();
    }
}
