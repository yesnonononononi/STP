package com.summit.stp.coupon.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.summit.stp.coupon.domain.model.CouponActivity;
import com.summit.stp.coupon.domain.repository.CouponActivityRepository;
import com.summit.stp.coupon.infrastructure.persistence.mapper.CouponActivityMapper;
import com.summit.stp.coupon.infrastructure.persistence.po.CouponActivityPO;
import com.summit.stp.shared.exception.ParameterException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class CouponActivityRepositoryImpl implements CouponActivityRepository {
    private final CouponActivityMapper couponActivityMapper;

    @Override
    public CouponActivity findById(Long id) {
        CouponActivityPO po = couponActivityMapper.selectById(id);
        if (po == null) {
            return null;
        }
        return convertToModel(po);
    }

    @Override
    public List<CouponActivity> findAll() {
        return couponActivityMapper.selectList(null).stream()
                .map(this::convertToModel)
                .collect(Collectors.toList());
    }

    @Override
    public void save(CouponActivity activity) {
        CouponActivityPO po = convertToPO(activity);
        couponActivityMapper.insert(po);
    }

    @Override
    public void update(CouponActivity activity) {
        CouponActivityPO po = convertToPO(activity);
        couponActivityMapper.updateById(po);
    }

    @Override
    public void delete(Long id) {
        couponActivityMapper.deleteById(id);
    }

    private CouponActivity convertToModel(CouponActivityPO po) {
        return CouponActivity.builder()
                .id(po.getId())
                .couponId(po.getCouponId())
                .name(po.getName())
                .stock(po.getStock())
                .type(CouponActivity.Type.fromCode(po.getType()))
                .activityStartTime(po.getActivityStartTime())
                .activityEndTime(po.getActivityEndTime())
                .status(po.getStatus())
                .limitQuantity(po.getLimitQuantity())
                .build();
    }

    private CouponActivityPO convertToPO(CouponActivity model) {
        CouponActivity.Type type = model.getType();
        if(type == null) throw new ParameterException("优惠券活动类型不能为空");
        return CouponActivityPO.builder()
                .id(model.getId())
                .couponId(model.getCouponId())
                .type(type.getCode())
                .name(model.getName())
                .stock(model.getStock())
                .activityStartTime(model.getActivityStartTime())
                .activityEndTime(model.getActivityEndTime())
                .status(model.getStatus())
                .limitQuantity(model.getLimitQuantity())
                .build();
    }

    @Override
    public void updateStockByActivityId(Object activityId, Object stock) {
        int update = couponActivityMapper.update(new LambdaUpdateWrapper<CouponActivityPO>().eq(CouponActivityPO::getId, activityId).set(CouponActivityPO::getStock, stock));
        if (update != 1) {
             throw new RuntimeException(String.format("更新活动:%s 库存: %s 失败",activityId,stock));
        }
    }

    @Override
    public void batchUpdate(Map<Object, Object> stockOfActivities) {
        couponActivityMapper.batchUpdate(stockOfActivities);
    }

    @Override
    public List<CouponActivity> findByScopeType(Integer scopeType) {
        return couponActivityMapper.selectByScopeType(scopeType).stream().map(this::convertToModel).toList();
    }


}
