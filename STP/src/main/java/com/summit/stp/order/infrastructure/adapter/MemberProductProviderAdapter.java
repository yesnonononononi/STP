package com.summit.stp.order.infrastructure.adapter;

import com.summit.stp.member.application.service.MemberAppService;
import com.summit.stp.member.application.vo.MemberVO;
import com.summit.stp.order.application.service.ProductProvider;
import com.summit.stp.order.application.vo.ProductVO;
import com.summit.stp.shared.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.summit.stp.shared.exception.ParameterException;

/**
 * 针对会员套餐的商品信息查询适配器（防腐层 ACL 实现）
 */


@Component
@RequiredArgsConstructor
public class MemberProductProviderAdapter implements ProductProvider {

    private final MemberAppService memberAppService;

    @Override
    public ProductVO getProductInfo(Long productId) {
        Result<MemberVO> result = memberAppService.queryMemberById(productId);
        if (result == null || !result.isSuccess() || result.getData() == null) {
            throw new ParameterException("商品不存在或已下架，ID: " + productId);
        }
        MemberVO memberVO = result.getData();
        return ProductVO.builder()
                .id(memberVO.getId())
                .name(memberVO.getName())
                .price(memberVO.getPrice())
                .build();
    }
}
