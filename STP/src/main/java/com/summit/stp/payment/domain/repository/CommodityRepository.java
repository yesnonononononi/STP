package com.summit.stp.payment.domain.repository;

import com.summit.stp.payment.domain.model.Commodity;

public interface CommodityRepository {
    /**
     * 根据商品ID查询商品
     * @param id 商品ID
     * @return 商品(不存在抛出异常)
     */
    public Commodity findCommodityById(Long id);


}
