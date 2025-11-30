package com.kt.dto.order;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class OrderRequest {

	public record Create(

		@NotNull
		Long productId,

		@NotNull
		String receiverName,

		@NotNull
		String receiverAddress,

		@NotNull
		String receiverMobile,

		@Min(value=1)
		@Max(value=999)
		int quantity

	){

	}

}
