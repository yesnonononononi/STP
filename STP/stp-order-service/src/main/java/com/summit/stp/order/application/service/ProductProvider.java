package com.summit.stp.order.application.service;

import com.summit.stp.order.application.vo.ProductVO;

/**
 * 订单模块商品信息查询的端口接口（Port）
 */
public interface ProductProvider {
    /**
     * 根据商品ID获取通用的商品/套餐信息
     *
     * @param productId 商品ID
     * @return 统一的商品信息承载对象
     */
    ProductVO getProductInfo(Long productId);
}
