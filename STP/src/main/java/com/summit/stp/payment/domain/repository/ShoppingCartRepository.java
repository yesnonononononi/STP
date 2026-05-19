package com.summit.stp.payment.domain.repository;

import com.summit.stp.payment.domain.model.ShoppingCart;

public interface ShoppingCartRepository {
    ShoppingCart findShoppingCartById(Long shoppingCartId);
}
