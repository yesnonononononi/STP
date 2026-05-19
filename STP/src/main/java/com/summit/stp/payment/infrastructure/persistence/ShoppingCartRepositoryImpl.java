package com.summit.stp.payment.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.summit.stp.payment.domain.model.Commodity;
import com.summit.stp.payment.domain.model.ShoppingCart;
import com.summit.stp.payment.domain.repository.CommodityRepository;
import com.summit.stp.payment.domain.repository.ShoppingCartRepository;
import com.summit.stp.payment.infrastructure.persistence.mapper.ShoppingCartMapper;
import com.summit.stp.payment.infrastructure.persistence.po.ShoppingCartPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ShoppingCartRepositoryImpl implements ShoppingCartRepository {
    private final ShoppingCartMapper shoppingCartMapper;
    private final CommodityRepository commodityRepository;

    @Override
    public ShoppingCart findShoppingCartById(Long shoppingCartId) {

        // 1. 获取购物车基础信息
        ShoppingCartPO mainPo = shoppingCartMapper.selectById(shoppingCartId);
        if (mainPo == null) throw new RuntimeException("购物车不存在");;


        // 2. 查询该购物车下的所有项
        List<ShoppingCartPO> pos = shoppingCartMapper.selectList(new LambdaQueryWrapper<ShoppingCartPO>().eq(ShoppingCartPO::getId, mainPo.getId()));


        // 3. 组装 CartItem
        List<ShoppingCart.CartItem> items = pos.stream().map(po -> {
            Commodity commodity = commodityRepository.findCommodityById(po.getCommodityId());
            return ShoppingCart.CartItem.builder()
                    .commodity(commodity)
                    .quantity(po.getQuantity())
                    .build();
        }).collect(Collectors.toList());

        // 4. 返回聚合根
        return ShoppingCart.builder()
                .id(mainPo.getId())
                .uname(String.valueOf(mainPo.getUname()))
                .items(items)
                .createTime(mainPo.getCreateTime())
                .updateTime(mainPo.getUpdateTime())
                .build();
    }
}
