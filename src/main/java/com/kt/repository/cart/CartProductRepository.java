package com.kt.repository.cart;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kt.domain.cart.Cart;
import com.kt.domain.cartproduct.CartProduct;

public interface CartProductRepository extends JpaRepository<CartProduct,Long> {

	//장바구니에 들어갈 상품을 저장하거나 조회하는 역할

	Optional<CartProduct> findByCartIdAndProductId(Long cartId, Long ProductId);
}
