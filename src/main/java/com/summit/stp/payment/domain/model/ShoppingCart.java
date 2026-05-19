package com.summit.stp.payment.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

@Builder
@Getter
public class ShoppingCart {
    private final Long id;
    private final String uname;
    private final List<CartItem> items;
    private final Timestamp createTime;
    private final Timestamp updateTime;

    @Getter
    @Builder
    public static class CartItem {
        private final Commodity commodity;
        private final Integer quantity;

        public BigDecimal getSubTotal() {
            if (commodity == null || quantity == null) return BigDecimal.ZERO;
            return commodity.getPrice().multiply(new BigDecimal(quantity));
        }
    }

    /**
     * 计算购物车总价
     */
    public BigDecimal calculateTotal() {
        if (items == null || items.isEmpty()) return BigDecimal.ZERO;
        return items.stream()
                .map(CartItem::getSubTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
