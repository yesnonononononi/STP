package com.summit.stp.user.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.summit.devframeworkdddstarter.repo.AbstractRepository;
import com.summit.stp.user.domain.model.UserFollow;

import com.summit.stp.user.domain.model.UserFollowRepository;
import com.summit.stp.user.infrastructure.persistence.po.UserFollowPO;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
public class UserFollowRepositoryImpl extends AbstractRepository<UserFollow, UserFollowPO> implements UserFollowRepository<UserFollow> {

    public UserFollowRepositoryImpl(BaseMapper<UserFollowPO> baseMapper) {
        super(baseMapper);
    }

    @Override
    public Optional<UserFollow> findByFollowerAndFollowee(Long followerId, Long followeeId) {
        if (followerId == null || followeeId == null) return Optional.empty();
        UserFollowPO po = getBaseMapper().selectOne(
                new LambdaQueryWrapper<UserFollowPO>()
                        .eq(UserFollowPO::getFollowerId, followerId)
                        .eq(UserFollowPO::getFolloweeId, followeeId)
        );
        return Optional.ofNullable(toModel(po));
    }

    @Override
    public List<UserFollow> findByFollowerAndFollowees(Long followerId, Collection<Long> followeeIds) {
        if (followerId == null || followeeIds == null || followeeIds.isEmpty()) {
            return Collections.emptyList();
        }
        return getBaseMapper().selectList(
                new LambdaQueryWrapper<UserFollowPO>()
                        .eq(UserFollowPO::getFollowerId, followerId)
                        .in(UserFollowPO::getFolloweeId, followeeIds)
                        .eq(UserFollowPO::getStatus, 1)
        ).stream().map(this::toModel).toList();
    }

    @Override
    public void save(UserFollow userFollow) {
        super.save(userFollow);
    }

    @Override
    public void delete(Long id) {
        delete(id, UserFollowPO::getId);
    }

    @Override
    public Page<UserFollow> queryFollowersPage(Long followeeId, long page, long pageSize) {
        Page<UserFollowPO> poPage = getBaseMapper().selectPage(
                new Page<>(page, pageSize),
                new LambdaQueryWrapper<UserFollowPO>()
                        .eq(UserFollowPO::getFolloweeId, followeeId)
                        .eq(UserFollowPO::getStatus, 1)
                        .orderByDesc(UserFollowPO::getCreateTime)
        );
        Page<UserFollow> modelPage = new Page<>(poPage.getCurrent(), poPage.getSize(), poPage.getTotal());
        modelPage.setRecords(poPage.getRecords().stream().map(this::toModel).toList());
        return modelPage;
    }

    @Override
    public Page<UserFollow> queryFolloweesPage(Long followerId, long page, long pageSize) {
        Page<UserFollowPO> poPage = getBaseMapper().selectPage(
                new Page<>(page, pageSize),
                new LambdaQueryWrapper<UserFollowPO>()
                        .eq(UserFollowPO::getFollowerId, followerId)
                        .eq(UserFollowPO::getStatus, 1)
                        .orderByDesc(UserFollowPO::getCreateTime)
        );
        Page<UserFollow> modelPage = new Page<>(poPage.getCurrent(), poPage.getSize(), poPage.getTotal());
        modelPage.setRecords(poPage.getRecords().stream().map(this::toModel).toList());
        return modelPage;
    }

    @Override
    protected UserFollowPO toPO(UserFollow entity) {
        if (entity == null) return null;
        UserFollowPO po = new UserFollowPO();
        po.setId(entity.getId());
        po.setFollowerId(entity.getFollowerId());
        po.setFolloweeId(entity.getFolloweeId());
        po.setCreateTime(entity.getCreateTime());
        po.setStatus(1);
        return po;
    }

    @Override
    protected UserFollow toModel(UserFollowPO po) {
        if (po == null) return null;
        return UserFollow.builder()
                .id(po.getId())
                .followerId(po.getFollowerId())
                .followeeId(po.getFolloweeId())
                .createTime(po.getCreateTime())
                .build();
    }
}
