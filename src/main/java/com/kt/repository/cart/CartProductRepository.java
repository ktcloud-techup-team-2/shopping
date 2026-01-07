package com.kt.repository.cart;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kt.domain.cartproduct.CartProduct;

public interface CartProductRepository extends JpaRepository<CartProduct,Long>, CartProductRepositoryCustom {

	//장바구니에 들어갈 상품을 저장하거나 조회하는 역할
	Optional<CartProduct> findByCartIdAndProductId(Long cartId, Long ProductId);

	Optional<CartProduct> findByCartIdAndCart_UserId(Long cartId, Long userId);

	// 장바구니에 담긴 모든 상품 조회
	List<CartProduct> findAllByCartId(Long cartId);

	// 장바구니 비우기
	void deleteAllByCartId(Long cartId);

}
