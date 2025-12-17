package com.kt.dto.cart;

import java.time.LocalDateTime;

import com.kt.domain.cartproduct.CartProduct;

public class CartResponse {

	public record Create(

		String name,
		int price,
		int count

	){
		public static Create from(CartProduct cartProduct){

			return new Create(

				cartProduct.getProduct().getName(),
				cartProduct.getProduct().getPrice(),
				cartProduct.getCount()

			);
		}

	}

	public record Detail(

		Long cartId,
		Long productId,
		String name,
		int price,
		int count,
		LocalDateTime createdAt,
		LocalDateTime updatedAt
		//Long productPrice, //상품별 금액
		//Long totalPrice //전체 금액

	){

	}

	public record CountUpdate(

		Long productId,
		String name,
		int count,
		LocalDateTime updatedAt
	){
	}
}
