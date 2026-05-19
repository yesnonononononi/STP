package com.summit.stp.payment.infrastructure.persistence;

import com.summit.stp.payment.domain.exception.NoSuchCommodityException;
import com.summit.stp.payment.domain.model.Commodity;
import com.summit.stp.payment.domain.repository.CommodityRepository;
import com.summit.stp.payment.infrastructure.persistence.mapper.CommodityMapper;
import com.summit.stp.payment.infrastructure.persistence.po.CommodityPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CommodityRepositoryImpl implements CommodityRepository {
    private final CommodityMapper commodityMapper;

    @Override
    public Commodity findCommodityById(Long id) {
        CommodityPO commodityPO = commodityMapper.selectById(id);
        Commodity commodity = Commodity.of(commodityPO.getId(), commodityPO.getName(), commodityPO.getPrice());
        return Optional.of(commodity).orElseThrow(NoSuchCommodityException::new);
    }


}
