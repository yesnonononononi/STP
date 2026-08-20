package com.summit.stp.coupon.infrastructure.persistence;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.summit.devframeworkdddstarter.repo.AbstractRepository;
import com.summit.stp.coupon.domain.model.Coupon;
import com.summit.stp.coupon.domain.repository.CouponRepository;
import com.summit.stp.coupon.infrastructure.persistence.mapper.CouponUseScopeMapper;
import com.summit.stp.coupon.infrastructure.persistence.po.CouponPO;
import com.summit.stp.coupon.infrastructure.persistence.po.CouponUseScopePO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class CouponRepositoryImpl extends AbstractRepository<Coupon, CouponPO> implements CouponRepository {

    public CouponRepositoryImpl(BaseMapper<CouponPO> baseMapper ) {
        super(baseMapper);

    }

    @Override
    public void update(Coupon template) {
        if (template == null) return;
        updateById(template);
    }

    @Override
    public List<Coupon> findByIds(List<Long> cList) {
        return findListIn(cList, CouponPO::getId);
    }

    @Override
    public Page<Coupon> list(String keyword, Integer status, Integer page, Integer pageSize) {
        Page<CouponPO> poPage = new Page<>(page,pageSize);
        Page<Coupon> res = new Page<>();
        LambdaQueryWrapper<CouponPO> queryWrapper = new LambdaQueryWrapper<>();
        if(StrUtil.isNotBlank(keyword))queryWrapper.likeRight(CouponPO::getName, keyword);
        if(status != null)queryWrapper.eq(CouponPO::getStatus, status);
        poPage = getBaseMapper().selectPage(poPage, queryWrapper);
        return res.setTotal(poPage.getTotal()).setRecords(poPage.getRecords().stream().map(this::toModel).toList()).setCurrent(poPage.getCurrent());
    }

    @Override
    public void deleteById(Long id) {
        delete(id, CouponPO::getId);
    }

    @Override
    protected CouponPO toPO(Coupon domain) {
        if (domain == null) return null;
        CouponPO po = CouponPO.builder().build();
        BeanUtil.copyProperties(domain, po);
        po.setId(domain.getId());
        po.setTimeType(domain.getTimeType() != null ? domain.getTimeType().getCode() : null);
        po.setScopeType(domain.getScopeType() != null ? domain.getScopeType().getCode() : null);
        return po;
    }

    @Override
    protected Coupon toModel(CouponPO couponPO) {
        if (couponPO == null) return null;
        return Coupon.builder()
                .id(couponPO.getId())
                .name(couponPO.getName())
                .amount(couponPO.getAmount())
                .discount(couponPO.getDiscount())
                .type(couponPO.getType())
                .createTime(couponPO.getCreateTime())
                .status(couponPO.getStatus())
                .timeType(couponPO.getTimeType() != null ? Coupon.CouponDateType.getByCode(couponPO.getTimeType()) : null)
                .description(couponPO.getDescription())
                .validDays(couponPO.getValidDays())
                .validHours(couponPO.getValidHours())
                .scopeType(couponPO.getScopeType() != null ? Coupon.CouponScopeType.fromCode(couponPO.getScopeType()) : null)
                .build();
    }
}

