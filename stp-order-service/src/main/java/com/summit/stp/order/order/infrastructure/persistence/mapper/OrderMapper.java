package com.summit.stp.order.order.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.summit.stp.order.order.infrastructure.persistence.dto.OrderOverviewStatDTO;
import com.summit.stp.order.order.infrastructure.persistence.dto.OrderTrendStatDTO;
import com.summit.stp.order.order.infrastructure.persistence.po.OrderPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.sql.Timestamp;
import java.util.List;

@Mapper
public interface OrderMapper extends BaseMapper<OrderPO> {

    @Update( "UPDATE payment_order po set po.status = #{status} where po.id=#{orderNo} and po.status = #{expectStatus}")
    void ackOrderIfWaitPay(Long orderNo,Integer status,Integer expectStatus);

    OrderOverviewStatDTO selectOrderOverviewStats(@Param("startOfToday") Timestamp startOfToday, @Param("startOfYesterday") Timestamp startOfYesterday);

    List<OrderTrendStatDTO> selectOrderTrendsStats(@Param("startTimestamp") Timestamp startTimestamp);
}
