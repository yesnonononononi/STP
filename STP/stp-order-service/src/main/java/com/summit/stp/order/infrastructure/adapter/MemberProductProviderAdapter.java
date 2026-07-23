package com.summit.stp.order.infrastructure.adapter;

import com.summit.stp.shared.application.vo.MemberVO;
import com.summit.stp.order.application.service.ProductProvider;
import com.summit.stp.order.application.vo.ProductVO;
import com.summit.stp.shared.exception.ParameterException;
import com.summit.stp.shared.result.Result;
import com.summit.stp.common.feign.MemberFeignClient;
import lombok.RequiredArgsConstructor;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

/**
 * 针对会员套餐的商品信息查询适配器（防腐层 ACL 实现）
 */
@Component
@RequiredArgsConstructor
public class MemberProductProviderAdapter implements ProductProvider {

    private final MemberFeignClient memberFeignClient;

    @Override
    public ProductVO getProductInfo(Long productId) {
        Result<MemberVO> result = memberFeignClient.queryMemberById(productId);
        if (result == null || !result.isSuccess() || result.getData() == null) {
            throw new ParameterException("商品不存在或已下架，ID: " + productId);
        }
        MemberVO memberVO = result.getData();
        return ProductVO.builder()
                .id(memberVO.getId())
                .name(memberVO.getName())
                .price(memberVO.getPrice())
                .discount(memberVO.getDiscount() != null ? BigDecimal.valueOf(memberVO.getDiscount()) : BigDecimal.ONE)
                .build();
    }
}
