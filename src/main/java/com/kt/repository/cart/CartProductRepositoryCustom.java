package com.kt.repository.cart;

import java.util.List;

import com.kt.domain.cartproduct.CartProduct;
import com.kt.dto.cart.CartResponse;

public interface CartProductRepositoryCustom {

	//장바구니 상세 조회를 위한 메서드
	List<CartResponse.Detail> findCartDetailList(Long cartId);
}
