package com.kt.domain.order;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Table;

import com.kt.common.api.CustomException;
import com.kt.common.api.ErrorCode;
import com.kt.common.jpa.BaseTimeEntity;
import com.kt.domain.orderproduct.OrderProduct;
import com.kt.domain.payment.Payment;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "orders", uniqueConstraints = {
	@UniqueConstraint(name = "uk_order_number", columnNames = {"order_number"})
})
@NoArgsConstructor
public class Order extends BaseTimeEntity {

	@Column(nullable = false)
	private Long userId;

	@Column(nullable = false)
	private String orderNumber;

	@Embedded
	private Receiver receiver;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private OrderStatus orderStatus;

	@Column(nullable = false)
	private Long orderAmount;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private OrderType orderType;

	@OneToMany(mappedBy = "order")
	private final List<OrderProduct> orderProducts = new ArrayList<>();

	@OneToMany(mappedBy = "order", cascade = CascadeType.MERGE, orphanRemoval = true)
	private List<Payment> payments = new ArrayList<>();

	private Order(Long userId, Receiver receiver, String orderNumber, OrderType orderType) {
		this.userId = userId;
		this.receiver = receiver;
		this.orderAmount = 0L;
		this.orderStatus = OrderStatus.PENDING;
		this.orderNumber = orderNumber;
		this.orderType = orderType;
	}

	public static Order create(Long userId, Receiver receiver, String orderNumber, OrderType orderType) {
		return new Order(userId, receiver, orderNumber, orderType);
	}

	//장바구니 주문인지 확인
	public boolean isCartOrder() {
		return this.orderType == OrderType.CART;
	}

	public void mapToOrder(OrderProduct orderProduct) {
		orderProducts.add(orderProduct);
	}

	// 모든 주문 상품 추가 후 한 번만 호출
	public void calculateTotalAmount() {
		this.orderAmount = orderProducts.stream()
			.mapToLong(op -> (long) op.getProductPrice() * op.getQuantity())
			.sum();
	}

	//주문 취소
	public void cancel(){

		//이미 취소되었는지
		validateAlreadyCanceled();

		//사용자는 결제 대기(PENDING) 상태일 때만 취소 가능
		if (this.orderStatus != OrderStatus.PENDING) {
			throw new CustomException(ErrorCode.ORDER_CANCEL_NOT_ALLOWED);
		}

		this.orderStatus = OrderStatus.CANCELLED;

	}
	public void cancelAdmin() {
		//이미 취소되었는지
		validateAlreadyCanceled();

		this.orderStatus = OrderStatus.CANCELLED;
	}
	private void validateAlreadyCanceled() {
		if (this.orderStatus == OrderStatus.CANCELLED) {
			throw new CustomException(ErrorCode.ORDER_ALREADY_CANCELLED);
		}
	}
	//주문 수정 = 배송 정보 수정
	public void updateReceiver(Receiver receiver){

		//결제 대기 상태일때만 수정 가능
		if(this.orderStatus != OrderStatus.PENDING){
			throw new CustomException(ErrorCode.ORDER_NOT_MODIFIABLE);
		}
		this.receiver = receiver;
	}

	public void changeOrderStatus(OrderStatus status) {
		this.orderStatus = status;
	}

	// 결제 대기 상태인지 확인
	public boolean isOrderPending() {
		return this.orderStatus == OrderStatus.PENDING;
	}

	// 주문 완료 처리 (결제 승인 후)
	public void complete() {
		if (this.orderStatus != OrderStatus.PENDING) {
			throw new CustomException(ErrorCode.ORDER_NOT_PENDING);
		}
		this.orderStatus = OrderStatus.COMPLETED;
	}

	// 결제 추가 (양방향 관계 설정)
	public void addPayment(Payment payment) {
		this.payments.add(payment);
	}

	// 최신 결제 조회 (가장 최근에 추가된 결제)
	public Payment getLatestPayment() {
		if (payments.isEmpty()) {
			return null;
		}
		return payments.get(payments.size() - 1);
	}

}