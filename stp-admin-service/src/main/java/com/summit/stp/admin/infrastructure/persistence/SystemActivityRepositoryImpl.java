package com.summit.stp.admin.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.summit.devframeworkdddstarter.repo.AbstractRepository;
import com.summit.stp.admin.domain.model.SystemActivity;
import com.summit.stp.admin.domain.model.SystemActivityRepository;
import com.summit.stp.admin.domain.model.SystemActivityTypeEnum;
import com.summit.stp.admin.infrastructure.persistence.mapper.SystemActivityMapper;
import com.summit.stp.admin.infrastructure.persistence.po.SystemActivityPO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class SystemActivityRepositoryImpl extends AbstractRepository<SystemActivity, SystemActivityPO> implements SystemActivityRepository<SystemActivity> {

    public SystemActivityRepositoryImpl(SystemActivityMapper baseMapper) {
        super(baseMapper);
    }

    @Override
    public void save(SystemActivity activity) {
        super.save(activity);
    }

    @Override
    public Optional<SystemActivity> findById(Long id) {
        if (id == null) return Optional.empty();
        SystemActivityPO po = getBaseMapper().selectById(id);
        return Optional.ofNullable(toModel(po));
    }

    @Override
    public List<SystemActivity> findRecentActivities(int limit) {
        LambdaQueryWrapper<SystemActivityPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(SystemActivityPO::getCreateTime)
               .last("LIMIT " + Math.max(limit, 1));
        List<SystemActivityPO> poList = getBaseMapper().selectList(wrapper);
        return poList.stream().map(this::toModel).toList();
    }

    @Override
    public Page<SystemActivity> queryActivitiesPage(String type, String module, long page, long pageSize) {
        LambdaQueryWrapper<SystemActivityPO> wrapper = new LambdaQueryWrapper<>();
        if (type != null && !type.isBlank()) {
            wrapper.eq(SystemActivityPO::getType, type);
        }
        if (module != null && !module.isBlank()) {
            wrapper.eq(SystemActivityPO::getModule, module);
        }
        wrapper.orderByDesc(SystemActivityPO::getCreateTime);

        Page<SystemActivityPO> poPage = getBaseMapper().selectPage(new Page<>(page, pageSize), wrapper);
        Page<SystemActivity> modelPage = new Page<>(poPage.getCurrent(), poPage.getSize(), poPage.getTotal());
        modelPage.setRecords(poPage.getRecords().stream().map(this::toModel).toList());
        return modelPage;
    }

    @Override
    protected SystemActivityPO toPO(SystemActivity entity) {
        if (entity == null) return null;
        SystemActivityPO po = new SystemActivityPO();
        po.setId(entity.getId());
        po.setType(entity.getType() != null ? entity.getType().getCode() : SystemActivityTypeEnum.INFO.getCode());
        po.setModule(entity.getModule());
        po.setTitle(entity.getTitle());
        po.setContent(entity.getContent());
        po.setTargetUrl(entity.getTargetUrl());
        po.setCreateTime(entity.getCreateTime());
        return po;
    }

    @Override
    protected SystemActivity toModel(SystemActivityPO po) {
        if (po == null) return null;
        return SystemActivity.builder()
                .id(po.getId())
                .type(SystemActivityTypeEnum.fromCode(po.getType()))
                .module(po.getModule())
                .title(po.getTitle())
                .content(po.getContent())
                .targetUrl(po.getTargetUrl())
                .createTime(po.getCreateTime())
                .build();
    }
}
