package com.kt.domain.payment;

import com.kt.common.api.CustomException;
import com.kt.common.api.ErrorCode;
import com.kt.common.jpa.BaseTimeEntity;
import com.kt.domain.order.Order;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "payments", uniqueConstraints = {
	// 토스에서 받은 결제 키는 유일해야 함 (중복 승인 방지 멱등키)
	@UniqueConstraint(name = "uk_payment_key", columnNames = {"payment_key"})
})
// 주문:결제 = 1:N (결제 실패 후 재시도 가능)
public class Payment extends BaseTimeEntity {

	//결제키 (결제 승인 후 설정됨)
	@Column
	private String paymentKey;

	@Column(nullable = false)
	private Long userId;

	@Column(nullable = false)
	private String orderNumber;

	@Column(nullable = false)
	private Long orderAmount;

	@Column(nullable = false)
	private Long deliveryFee;

	@Column(nullable = false)
	private Long paymentAmount;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PaymentType type;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PaymentStatus status;

	@Column
	private String cancelReason;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id")
	private Order order;

	private Payment(Long userId, Order order, Long deliveryFee, PaymentType type) {
		this.userId = userId;
		this.order = order;
		this.orderNumber = order.getOrderNumber();
		this.orderAmount = order.getOrderAmount();
		this.deliveryFee = deliveryFee;
		this.paymentAmount = this.orderAmount + deliveryFee;
		this.type = type;
		this.status = PaymentStatus.READY;
		// 양방향 관계 설정
		order.addPayment(this);
	}

	public static Payment create(Long userId, Order order, Long deliveryFee, PaymentType type) {
		return new Payment(userId, order, deliveryFee, type);
	}


	//결제 승인 확정
	//외부 PG사(토스)로부터 받은 결제 키를 저장하고 상태를 DONE으로 변경
	public void confirmPayment(String paymentKey) {
		this.paymentKey = paymentKey;
		this.status = PaymentStatus.DONE; // 상태를 '결제 완료'로 변경
	}

	//결제 실패
	public void failPayment() {
		this.status = PaymentStatus.FAILED;
	}


	//결제 취소
	public void cancel(String cancelReason) {
		if (this.status == PaymentStatus.CANCELED) {
			throw new CustomException(ErrorCode.PAYMENT_ALREADY_CANCELLED);
		}
		if (!isCancelable()) {
			throw new CustomException(ErrorCode.PAYMENT_CANNOT_CANCEL);
		}
		this.status = PaymentStatus.CANCELED;
		this.cancelReason = cancelReason;
	}

	//취소 가능 여부 체크 (DONE 상태만 취소 가능)
	public boolean isCancelable() {
		return this.status == PaymentStatus.DONE;
	}

	public boolean isReady() {
		return this.status == PaymentStatus.READY;
	}

	public void changeStatus(PaymentStatus status) {
		this.status = status;
	}
}
