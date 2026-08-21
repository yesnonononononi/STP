package com.summit.stp.user.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.summit.devframeworkdddstarter.repo.AbstractRepository;
import com.summit.stp.user.domain.model.UserFollow;

import com.summit.stp.user.domain.repository.UserFollowRepository;
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
    public Page<UserFollow> queryFolloweesPage(Long followeeId, long page, long pageSize) {
        Page<UserFollow> res = new Page<>();
        Page<UserFollowPO> userFollowPOPage = new Page<>(page,pageSize);
        getBaseMapper().selectPage(userFollowPOPage,new LambdaQueryWrapper<UserFollowPO>().eq(UserFollowPO::getFolloweeId,followeeId).orderByDesc(UserFollowPO::getCreateTime).ne(UserFollowPO::getStatus,UserFollow.FollowStatus.CANCEL.getCode()));
        return res.setCurrent(userFollowPOPage.getCurrent()).setTotal(userFollowPOPage.getTotal()).setRecords(userFollowPOPage.getRecords().stream().map(this::toModel).toList());
    }

    @Override
    protected UserFollowPO toPO(UserFollow entity) {
        if (entity == null) return null;
        return UserFollowPO.builder()
                .id(entity.getId())
                .followerId(entity.getFollowerId())
                .followeeId(entity.getFolloweeId())
                .status(entity.getStatus().getCode())
                .source(entity.getSource())
                .build();

    }

    @Override
    protected UserFollow toModel(UserFollowPO po) {
        if (po == null) return null;
        return UserFollow.builder()
                .id(po.getId())
                .status(UserFollow.FollowStatus.fromCode(po.getStatus()))
                .source(po.getSource())
                .followerId(po.getFollowerId())
                .followeeId(po.getFolloweeId())
                .createTime(po.getCreateTime())
                .build();
    }
}
