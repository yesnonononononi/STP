package com.summit.stp.coupon.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.summit.stp.common.application.domain.exception.ParameterException;
import com.summit.stp.coupon.domain.model.CouponActivity;
import com.summit.stp.coupon.domain.repository.CouponActivityRepository;
import com.summit.stp.coupon.infrastructure.persistence.mapper.CouponActivityMapper;
import com.summit.stp.coupon.infrastructure.persistence.po.CouponActivityPO;
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
        CouponActivityPO po = couponActivityMapper.selectOne(new LambdaQueryWrapper<CouponActivityPO>()
                .eq(CouponActivityPO::getPublicId, id));
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
        couponActivityMapper.update(po, new LambdaUpdateWrapper<CouponActivityPO>()
                .eq(CouponActivityPO::getPublicId, activity.getId()));
    }

    @Override
    public void delete(Long id) {
        couponActivityMapper.delete(new LambdaQueryWrapper<CouponActivityPO>()
                .eq(CouponActivityPO::getPublicId, id));
    }

    private CouponActivity convertToModel(CouponActivityPO po) {
        return CouponActivity.builder()
                .id(po.getPublicId())
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
                .publicId(model.getId())
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
        int update = couponActivityMapper.update(new CouponActivityPO(), new LambdaUpdateWrapper<CouponActivityPO>()
                .eq(CouponActivityPO::getPublicId, activityId)
                .set(CouponActivityPO::getStock, stock));
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
        List<CouponActivityPO> list = couponActivityMapper.selectByScopeType(scopeType);
        return list.stream().map(this::convertToModel).toList();
    }


}
