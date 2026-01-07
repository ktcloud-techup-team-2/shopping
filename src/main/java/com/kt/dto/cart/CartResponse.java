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
		int previousCount,
		int newCount,
		int unitPrice,
		int totalPrice,
		String message,
		LocalDateTime updatedAt
	){
		public static CountUpdate of(CartProduct cartProduct, int previousCount) {
			int newCount = cartProduct.getCount();
			int unitPrice = cartProduct.getProduct().getPrice();
			String message = previousCount < newCount 
				? String.format("수량이 %d개에서 %d개로 증가했습니다.", previousCount, newCount)
				: String.format("수량이 %d개에서 %d개로 감소했습니다.", previousCount, newCount);
			
			return new CountUpdate(
				cartProduct.getProduct().getId(),
				cartProduct.getProduct().getName(),
				previousCount,
				newCount,
				unitPrice,
				unitPrice * newCount,
				message,
				cartProduct.getUpdatedAt()
			);
		}
	}
}
