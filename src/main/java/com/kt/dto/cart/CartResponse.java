package com.kt.dto.cart;

import com.kt.domain.cartproduct.CartProduct;
import com.kt.domain.product.Product;

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
}
