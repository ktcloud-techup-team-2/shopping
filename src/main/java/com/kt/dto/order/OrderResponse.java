package com.kt.dto.order;

import java.time.LocalDateTime;
import java.util.List;

import com.kt.domain.order.Order;
import com.kt.domain.order.OrderStatus;
import com.kt.domain.order.OrderType;
import com.kt.domain.order.Receiver;
import com.kt.domain.orderproduct.OrderProduct;
import com.kt.domain.payment.Payment;
import com.kt.domain.payment.PaymentStatus;

public interface OrderResponse {

	// 주문 생성 응답
	record Create(
		String orderNumber,
		String orderType,
		String orderStatusDescription,  // 상태 설명 추가
		Long orderAmount,
		Long deliveryFee,
		Long paymentAmount,
		LocalDateTime createdAt,
		String message// 사용자 친화적 메시지
	){
		public static Create from(Order order){
			String message = order.getOrderType() == OrderType.CART
				? "장바구니 상품으로 주문이 생성되었습니다."
				: "바로 주문이 생성되었습니다.";
			return new Create(
				order.getOrderNumber(),
				order.getOrderType().name(),
				getStatusDescription(order.getOrderStatus()),
				order.getOrderAmount(),
				order.getLatestPayment().getDeliveryFee(),
				order.getLatestPayment().getPaymentAmount(),
				order.getCreatedAt(),
				message
			);
		}
		private static String getStatusDescription(OrderStatus status) {
			return switch (status) {
				case PENDING -> "결제 대기중";
				case COMPLETED -> "결제 완료";
				case SHIPPED -> "배송중";
				case DELIVERED -> "배송 완료";
				case CANCELLED -> "주문 취소";
				default -> status.name();
			};
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

	// 결제 요청 시작 응답 (프론트가 PG사 호출할때 사용함)
	record PaymentReady(
		String orderNumber,       // PG사 orderId로 사용
		Long amount,
		String paymentType,
		PaymentStatus status,
		LocalDateTime createdAt
	) {
		public static PaymentReady from(Payment payment) {
			return new PaymentReady(
				payment.getOrderNumber(),
				payment.getPaymentAmount(),
				payment.getType().name(),
				payment.getStatus(),
				payment.getCreatedAt()
			);
		}
	}

	// 결제 승인 성공 응답
	record PaymentConfirm(
		String orderNumber,
		Long amount,
		String paymentKey,
		PaymentStatus status,
		LocalDateTime approvedAt
	) {
		public static PaymentConfirm from(Payment payment) {
			return new PaymentConfirm(
				payment.getOrderNumber(),
				payment.getPaymentAmount(),
				payment.getPaymentKey(),
				payment.getStatus(),
				LocalDateTime.now()
			);
		}
	}

	// 결제 실패 응답
	record PaymentFail(
		String orderNumber,
		PaymentStatus status,
		String errorCode,
		String errorMessage
	) {
		public static PaymentFail of(String orderNumber, PaymentStatus status, String errorCode, String errorMessage) {
			return new PaymentFail(orderNumber, status, errorCode, errorMessage);
		}
	}
}