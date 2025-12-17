package com.kt.repository.cart;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kt.domain.cartproduct.CartProduct;
import com.kt.repository.category.CategoryRepositoryCustom;

//JPA가 제공하는 기본 메서드를 사용하는 메인 Repository
public interface CartProductRepository extends JpaRepository<CartProduct,Long>, CategoryRepositoryCustom {

	//장바구니에 들어갈 상품을 저장하거나 조회하는 역할
	Optional<CartProduct> findByCartIdAndProductId(Long cartId, Long ProductId);

	Optional<CartProduct> findByCartIdAndCart_UserId(Long cartId, Long userId);

}
