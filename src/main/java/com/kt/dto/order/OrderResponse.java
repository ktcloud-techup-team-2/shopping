package com.kt.dto.order;

import java.time.LocalDateTime;

import com.kt.domain.order.Order;
import com.kt.domain.order.OrderStatus;

public class OrderResponse {

	public record Create(

		Long id,
		String orderNumber,
		OrderStatus orderStatus,
		long totalPaymentAmount,
		LocalDateTime createdAt

	){
		public static Create from(Order order){
			return new Create(
				order.getId(),
				order. getOrderNumber(),
				order.getOrderStatus(),
				order.getTotalPaymentAmount(),
				order.getCreatedAt()
			);
		}
	}
}
