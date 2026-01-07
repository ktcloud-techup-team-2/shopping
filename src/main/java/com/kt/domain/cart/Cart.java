package com.kt.domain.cart;

import java.util.ArrayList;
import java.util.List;

import com.kt.common.jpa.BaseTimeEntity;
import com.kt.domain.cartproduct.CartProduct;
import com.kt.domain.user.User;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "cart")
@Entity
@Getter
@NoArgsConstructor
public class Cart extends BaseTimeEntity {

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<CartProduct> products = new ArrayList<>();

	private Cart(Long userId){
		this.userId = userId;
	}

	/**
	 * User 객체로 Cart 생성
	 */
	public static Cart create(User user){
		return new Cart(user.getId());
	}

	/**
	 * userId로 Cart 생성
	 */
	public static Cart create(Long userId){
		return new Cart(userId);
	}
}
