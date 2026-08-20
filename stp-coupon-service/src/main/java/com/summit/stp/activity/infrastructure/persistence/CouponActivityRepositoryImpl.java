package com.summit.stp.activity.infrastructure.persistence;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.summit.stp.activity.domain.model.CouponActivity;
import com.summit.stp.activity.domain.repository.CouponActivityRepository;
import com.summit.stp.activity.infrastructure.persistence.mapper.CouponActivityMapper;
import com.summit.stp.activity.infrastructure.persistence.po.CouponActivityPO;
import com.summit.stp.common.application.domain.exception.BusinessException;
import com.summit.stp.common.application.domain.exception.ParameterException;
import com.summit.devframeworkdddstarter.repo.AbstractRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Repository
public class CouponActivityRepositoryImpl extends AbstractRepository<CouponActivity, CouponActivityPO> implements CouponActivityRepository {
    private final CouponActivityMapper couponActivityMapper;

    public CouponActivityRepositoryImpl(BaseMapper<CouponActivityPO> baseMapper, CouponActivityMapper couponActivityMapper) {
        super(baseMapper);
        this.couponActivityMapper = couponActivityMapper;
    }




    @Override
    public void update(CouponActivity activity) {
        if (activity == null) return;
        updateById(activity);
    }

    @Override
    public void delete(Long id) {
        delete(id, CouponActivityPO::getId);
    }

    @Override
    public void updateStockByActivityId(Object activityId, Object stock) {
        int update = getBaseMapper().update(new CouponActivityPO(), new LambdaUpdateWrapper<CouponActivityPO>()
                .eq(CouponActivityPO::getId, activityId)
                .set(CouponActivityPO::getStock, stock));
        if (update != 1) {
            throw new RuntimeException(String.format("更新活动:%s 库存: %s 失败", activityId, stock));
        }
    }

    @Override
    public void batchUpdate(Map<Object, Object> stockOfActivities) {
        couponActivityMapper.batchUpdate(stockOfActivities);
    }

    @Override
    public List<CouponActivity> findByScopeType(Integer scopeType) {
        List<CouponActivityPO> list = couponActivityMapper.selectByScopeType(scopeType);
        return list.stream().map(this::toModel).toList();
    }

    @Override
    public Page<CouponActivity> list(String keyword, Integer status, Integer page, Integer pageSize) {
        Page<CouponActivityPO> p = new Page<>(page,pageSize);
        LambdaQueryWrapper<CouponActivityPO> queryWrapper = new LambdaQueryWrapper<>();
        if(StrUtil.isNotBlank(keyword))queryWrapper.likeRight(CouponActivityPO::getName, keyword);
        if(status != null)queryWrapper.eq(CouponActivityPO::getStatus, status);
        Page<CouponActivity> res = new Page<>();
        getBaseMapper().selectPage(p, queryWrapper);
        List<CouponActivity> records = p.getRecords().stream().map(this::toModel).toList();
        return res.setTotal(p.getTotal()).setRecords(records).setCurrent(p.getCurrent());
    }

    @Override
    public void deductStockWithOptimisticLock(Long couponId) {
        try {
            int row = couponActivityMapper.deductStockWithOptimisticLock(couponId);
            if(row == 0){
                throw new BusinessException("优惠券库存不足");
            }
        }catch (Exception e){
            log.error(" deduct stock with optimistic lock error ", e);
            throw e;
        }
    }

    @Override
    protected CouponActivity toModel(CouponActivityPO po) {
        if (po == null) return null;
        return CouponActivity.builder()
                .id(po.getId())
                .couponId(po.getCouponId())
                .name(po.getName())
                .stock(po.getStock())
                .type(po.getType() != null ? CouponActivity.Type.fromCode(po.getType()) : null)
                .activityStartTime(po.getActivityStartTime())
                .activityEndTime(po.getActivityEndTime())
                .status(po.getStatus())
                .createTime(po.getCreateTime())
                .build();
    }

    @Override
    protected CouponActivityPO toPO(CouponActivity model) {
        if (model == null) return null;
        CouponActivity.Type type = model.getType();
        if (type == null) {
            throw new ParameterException("优惠券活动类型不能为空");
        }
        return CouponActivityPO.builder()
                .id(model.getId())
                .couponId(model.getCouponId())
                .type(type.getCode())
                .name(model.getName())
                .stock(model.getStock())
                .activityStartTime(model.getActivityStartTime())
                .activityEndTime(model.getActivityEndTime())
                .status(model.getStatus())
                .createTime(model.getCreateTime())
                .build();
    }
}

