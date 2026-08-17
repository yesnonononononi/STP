package com.summit.stp.user.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.summit.devframeworkdddstarter.repo.AbstractRepository;
import com.summit.stp.user.domain.model.UserSetting;
import com.summit.stp.user.domain.repository.UserSettingRepository;
import com.summit.stp.user.infrastructure.persistence.po.UserSettingPO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Slf4j
@Repository
public class UserSettingRepositoryImpl extends AbstractRepository<UserSetting, UserSettingPO> implements UserSettingRepository<UserSetting> {

    public UserSettingRepositoryImpl(BaseMapper<UserSettingPO> baseMapper) {
        super(baseMapper);
    }

    @Override
    public void save(UserSetting userSetting) {
        super.save(userSetting);
    }

    @Override
    public void update(UserSetting userSetting) {
        if (userSetting == null || userSetting.getUserId() == null) return;
        updateById(userSetting);
    }

    @Override
    public Optional<UserSetting> findByUserId(Long userId) {
        return findBy(userId, UserSettingPO::getUserId);
    }

    @Override
    protected UserSettingPO toPO(UserSetting userSetting) {
        if (userSetting == null) return null;
        UserSettingPO po = new UserSettingPO();
        po.setId(userSetting.getId());
        po.setUserId(userSetting.getUserId());
        po.setShowDelPost(userSetting.getShowDelPost());
        po.setCustomizationRecommend(userSetting.getCustomizationRecommend());
        po.setCreateTime(userSetting.getCreateTime());
        po.setUpdateTime(userSetting.getUpdateTime());
        return po;
    }

    @Override
    protected UserSetting toModel(UserSettingPO po) {
        if (po == null) return null;
        return UserSetting.builder()
                .id(po.getId())
                .userId(po.getUserId())
                .showDelPost(po.getShowDelPost())
                .customizationRecommend(po.getCustomizationRecommend())
                .createTime(po.getCreateTime())
                .updateTime(po.getUpdateTime())
                .build();
    }
}
