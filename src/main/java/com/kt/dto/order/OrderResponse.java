package com.kt.dto.order;

import java.time.LocalDateTime;
import java.util.List;

import com.kt.domain.order.Order;
import com.kt.domain.order.OrderStatus;
import com.kt.domain.order.Receiver;
import com.kt.domain.orderproduct.OrderProduct;

public interface OrderResponse {

	record Create(
		String orderNumber,
		OrderStatus orderStatus,
		Long orderAmount,
		LocalDateTime createdAt
	){
		public static Create from(Order order){
			return new Create(
				order.getOrderNumber(),
				order.getOrderStatus(),
				order.getOrderAmount(),
				order.getCreatedAt()
			);
		}
	}

	//주문 목록 조회
	record OrderList(
		String orderNumber,
		OrderStatus orderStatus,
		Long orderAmount,
		LocalDateTime createdAt
	){
		public static OrderList from(Order order){
			return new OrderList(
				order.getOrderNumber(),
				order.getOrderStatus(),
				order.getOrderAmount(),
				order.getCreatedAt()
			);
		}
	}

	record MyOrder(
		String orderNumber,
		OrderStatus orderStatus,
		Long orderAmount,
		Receiver receiver,
		List<OrderedProduct> orderProducts,
		LocalDateTime createdAt
	){
		public static MyOrder from(Order order){
			List<OrderedProduct> products = order.getOrderProducts().stream()
				.map(orderProduct -> OrderedProduct.from(orderProduct))
				.toList();

			return new MyOrder(
				order.getOrderNumber(),
				order.getOrderStatus(),
				order.getOrderAmount(),
				order.getReceiver(),
				products,
				order.getCreatedAt()
			);
		}
	}

	record OrderedProduct(
		Long productId,
		int productPrice,
		int quantity,
		int orderPrice
	){
		public static OrderedProduct from(OrderProduct orderProduct){
			return new OrderedProduct(
				orderProduct.getProductId(),
				orderProduct.getProductPrice(),
				orderProduct.getQuantity(),
				orderProduct.getOrderPrice()
			);
		}
	}

	// 관리자 주문 목록 조회
	record AdminList(
		Long id,
		Long userId,
		String orderNumber,
		OrderStatus orderStatus,
		Long orderAmount,
		LocalDateTime createdAt
	){
		public static AdminList from(Order order){
			return new AdminList(
				order.getId(),
				order.getUserId(),
				order.getOrderNumber(),
				order.getOrderStatus(),
				order.getOrderAmount(),
				order.getCreatedAt()
			);
		}
	}

	// 관리자 주문 상세 조회
	record AdminDetail(
		Long id,
		Long userId,
		String orderNumber,
		OrderStatus orderStatus,
		long orderAmount,
		Receiver receiver,
		List<OrderedProduct> orderProducts,
		LocalDateTime createdAt
	){
		public static AdminDetail from(Order order){
			List<OrderedProduct> products = order.getOrderProducts().stream()
				.map(orderProduct -> OrderedProduct.from(orderProduct))
				.toList();

			return new AdminDetail(
				order.getId(),
				order.getUserId(),
				order.getOrderNumber(),
				order.getOrderStatus(),
				order.getOrderAmount(),
				order.getReceiver(),
				products,
				order.getCreatedAt()
			);
		}
	}
}