package com.summit.stp.message.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.summit.devframeworkdddstarter.repo.AbstractRepository;
import com.summit.stp.message.domain.model.UserSession;
import com.summit.stp.message.domain.repository.UserSessionRepository;
import com.summit.stp.message.infrastructure.persistence.po.UserSessionPO;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserSessionRepositoryImpl extends AbstractRepository<UserSession, UserSessionPO> implements UserSessionRepository {

    public UserSessionRepositoryImpl(BaseMapper<UserSessionPO> baseMapper) {
        super(baseMapper);
    }

    @Override
    public UserSession findByUserIdAndSessionId(Long userId, Long sessionId) {
        if (userId == null || sessionId == null) return null;
        LambdaQueryWrapper<UserSessionPO> queryWrapper = new LambdaQueryWrapper<UserSessionPO>()
                .eq(UserSessionPO::getUserId, userId)
                .eq(UserSessionPO::getSessionId, sessionId);
        UserSessionPO po = getBaseMapper().selectOne(queryWrapper);
        return po == null ? null : toModel(po);
    }

    @Override
    public UserSession findTargetSession(Long sessionId, Long userId) {
        if (sessionId == null || userId == null) return null;
        LambdaQueryWrapper<UserSessionPO> queryWrapper = new LambdaQueryWrapper<UserSessionPO>()
                .eq(UserSessionPO::getSessionId, sessionId)
                .ne(UserSessionPO::getUserId, userId)
                .last("limit 1");
        UserSessionPO po = getBaseMapper().selectOne(queryWrapper);
        return po == null ? null : toModel(po);
    }

    @Override
    public List<UserSession> findByUserId(Long userId) {
        return findListBy(userId, UserSessionPO::getUserId);
    }

    @Override
    public void save(UserSession userSession) {
        if (userSession == null) return;
        UserSessionPO po = toPO(userSession);
        UserSessionPO existing = findPOByUserIdAndSessionId(userSession.getUserId(), userSession.getSessionId());
        if (existing == null) {
            getBaseMapper().insert(po);
            return;
        }
        po.setId(existing.getId());
        getBaseMapper().updateById(po);
    }

    @Override
    public void batchUpdate(List<UserSession> list) {
        if (list == null || list.isEmpty()) return;
        list.forEach(this::save);
    }

    private UserSessionPO findPOByUserIdAndSessionId(Long userId, Long sessionId) {
        return getBaseMapper().selectOne(new LambdaQueryWrapper<UserSessionPO>()
                .eq(UserSessionPO::getUserId, userId)
                .eq(UserSessionPO::getSessionId, sessionId));
    }

    @Override
    public UserSessionPO toPO(UserSession userSession) {
        if (userSession == null) return null;
        return UserSessionPO.builder()
                .id(userSession.getId())
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

    @Override
    public UserSession toModel(UserSessionPO po) {
        if (po == null) return null;
        return UserSession.builder()
                .id(po.getId())
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
}

