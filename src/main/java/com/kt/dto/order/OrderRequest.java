package com.kt.dto.order;

import com.kt.domain.order.OrderStatus;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class OrderRequest {

	// 장바구니 주문 요청
	public record CartOrder(

		@NotNull
		String receiverName,

		@NotNull
		String receiverAddress,

		@NotNull
		@Pattern(regexp = "^(0\\d{1,2})-(\\d{3,4})-(\\d{4})$")
		String receiverMobile,

		@NotNull
		Long deliveryAddressId,

		@NotNull
		Integer deliveryFee
	) {
	}

	// 바로 주문 요청
	public record DirectOrder(
		@NotNull
		Long productId,

		@Min(value = 1)
		int quantity,

		@NotNull
		String receiverName,

		@NotNull
		String receiverAddress,

		@NotNull
		@Pattern(regexp = "^(0\\d{1,2})-(\\d{3,4})-(\\d{4})$")
		String receiverMobile,

		@NotNull
		Long deliveryAddressId,

		@NotNull
		Integer deliveryFee
	) {
	}

	public record Update(

		@NotNull
		String receiverName,

		@NotNull
		String receiverAddress,

		@NotNull
		@Pattern(regexp = "^(0\\d{1,2})-(\\d{3,4})-(\\d{4})$")
		String receiverMobile

	){

	}

	// 결제 요청 시작 (결제하기 버튼 클릭 시)
	public record StartPayment(
		@NotNull
		String orderNumber,

		@NotNull
		Long amount,

		@NotNull
		String paymentType
	) {
	}

	// 결제 승인 요청 (토스 콜백 successUrl로 리다이렉트 후)
	public record ConfirmPayment(
		@NotNull
		String paymentKey,     // 토스에서 발급한 결제 키

		@NotNull
		String orderNumber,    // 주문 번호

		@NotNull
		Long amount            // 결제 금액
	) {
	}

	// 관리자 주문 상태 변경
	public record ChangeStatus(
		@NotNull
		OrderStatus status
	){

	}

}
