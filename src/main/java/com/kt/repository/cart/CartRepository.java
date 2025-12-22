package com.kt.repository.cart;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kt.domain.cart.Cart;

public interface CartRepository extends JpaRepository<Cart,Long> {
	//회원의 Cart를 찾는 쿼리 메서드

	Optional<Cart> findByUserId(Long userId);

}
