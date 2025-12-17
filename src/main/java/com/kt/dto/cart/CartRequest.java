package com.kt.dto.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class CartRequest {

	public record Add(

		@NotNull
		Long productId,

		@Min(value = 1)
		int count //추가한 수량

	){

	}
}
