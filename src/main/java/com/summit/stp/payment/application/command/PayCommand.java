package com.summit.stp.payment.application.command;

import com.summit.stp.payment.infrastructure.Enum.PayType;
import lombok.Builder;
import lombok.Data;


@Builder
@Data
public class PayCommand {
    /**
     * 用户名
     */
    private String username;

    /**
     * 购物车ID
     */
    private Long shoppingCartId;



    /**
     * 支付方式
     */
    private PayType payType;




    /**
     * 优惠券ID
     */
    private Long couponId;





}
