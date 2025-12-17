package com.kt.domain.cartproduct;

import com.kt.common.jpa.BaseTimeEntity;
import com.kt.domain.cart.Cart;
import com.kt.domain.product.Product;

import jakarta.persistence.*;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "cart-product")
@Entity
@Getter
@NoArgsConstructor
public class CartProduct extends BaseTimeEntity {

	@ManyToOne
	private Cart cart;
	@ManyToOne
	private Product product;
	private int count;

	//총금액은 'Order'에서 계산

	private CartProduct(Cart cart, Product product, int count){

		this.cart = cart;
		this.product = product;
		this.count = count;

	}
	//장바구니에 담을 상품 엔티티 생성
	public static CartProduct create(Cart cart, Product product, int count){

		return new CartProduct(
			cart,
			product,
			count
		);
	}

	//장바구니에 담을 수량 증가
	public void add(int count){
		this.count += count;
	}
}
