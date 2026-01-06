package com.kt.service.order;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kt.common.api.CustomException;
import com.kt.common.api.ErrorCode;
import com.kt.domain.cart.Cart;
import com.kt.domain.cartproduct.CartProduct;
import com.kt.domain.order.Order;
import com.kt.domain.order.OrderType;
import com.kt.domain.order.Receiver;
import com.kt.domain.orderproduct.OrderProduct;
import com.kt.domain.payment.Payment;
import com.kt.domain.payment.PaymentType;
import com.kt.domain.product.Product;
import com.kt.dto.delivery.DeliveryRequest;
import com.kt.dto.order.OrderRequest;
import com.kt.repository.cart.CartProductRepository;
import com.kt.repository.cart.CartRepository;
import com.kt.repository.order.OrderRepository;
import com.kt.repository.orderproduct.OrderProductRepository;
import com.kt.repository.payment.PaymentRepository;
import com.kt.repository.product.ProductRepository;
import com.kt.service.delivery.DeliveryService;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {

	private final OrderRepository orderRepository;
	private final ProductRepository productRepository;
	private final OrderProductRepository orderProductRepository;
	private final DeliveryService deliveryService;
	private final CartRepository cartRepository;
	private final CartProductRepository cartProductRepository;
	private final PaymentRepository paymentRepository;
	private final OrderValidator orderValidator;
	private final OrderStockService orderStockService;

	// 주문번호 생성
	private String generateOrderNumber() {
		String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		Random random = new Random();
		StringBuilder randomNumber = new StringBuilder(5);
		for (int i = 0; i < 5; i++) {
			randomNumber.append(chars.charAt(random.nextInt(chars.length())));
		}
		return "ORD-" + date + randomNumber;
	}

	/** 주문 종류
	 * 1. 장바구니에서 주문하기 클릭 = 여러 상품들 주문
	 * 2. 상품 페이지에서 바로 주문 = 단일 상품 주문
	 */

	//1.  장바구니 주문 생성
	public Order createCartOrder(Long userId, OrderRequest.CartOrder request) {

		// 사용자의 장바구니 조회(한 사람 당 하나의 장바구니를 가지므로 userId로 조회)
		Cart cart = cartRepository.findByUserId(userId)
			.orElseThrow(() -> new CustomException(ErrorCode.CART_NOT_FOUND));

		// 장바구니에 담긴 상품들 조회
		List<CartProduct> cartProducts = cartProductRepository.findAllByCartId(cart.getId());

		// 주문 검증 (장바구니 비어있는지, 재고 검증)
		orderValidator.validateCartOrder(cartProducts);

		Receiver receiver = new Receiver(
			request.receiverName(),
			request.receiverAddress(),
			request.receiverMobile()
		);


		String orderNumber = generateOrderNumber();

		// 중복 조회
		orderRepository.findByOrderNumber(orderNumber)
			.ifPresent(o -> { throw new CustomException(ErrorCode.DUPLICATE_ORDER_NUMBER); });

		//주문 생성 (장바구니 주문)
		Order order = Order.create(userId, receiver, orderNumber, OrderType.CART);

		//주문 저장 전 동시성 제어(따닥 요청 방지, 동일한 주문번호의 주문이 저장되면 에러)
		try {
			orderRepository.save(order);
		}catch(DataIntegrityViolationException e){
			throw new CustomException(ErrorCode.DUPLICATE_ORDER_NUMBER);
		}


		// 주문 상품 생성
		for (CartProduct cartProduct : cartProducts) {
			Product product = cartProduct.getProduct();
			int quantity = cartProduct.getCount();

			OrderProduct orderProduct = OrderProduct.create(product, quantity, order);
			orderProductRepository.save(orderProduct);
			order.mapToOrder(orderProduct);
		}
		//총 금액 계산
		order.calculateTotalAmount();

		// 배송 정보 생성
		createDeliveryForOrder(order.getId(), request.deliveryAddressId(), request.deliveryFee());

		//이벤트 구현

		//DTO에서 받은 결제 수단을 Enum으로 변환
		PaymentType type = PaymentType.valueOf(request.paymentType());

		//결제 정보 생성
		Payment payment = Payment.create(
			userId,
			order,
			request.deliveryFee().longValue(),
			type
		);
		paymentRepository.save(payment);

		return order;
	}

	// 2. 바로 주문 생성
	public Order createDirectOrder(Long userId, OrderRequest.DirectOrder request) {
		// 상품 조회
		Product product = productRepository.findById(request.productId())
			.orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

		// 주문 검증 (재고 검증)
		orderValidator.validateDirectOrder(product.getId(), request.quantity());

		Receiver receiver = new Receiver(
			request.receiverName(),
			request.receiverAddress(),
			request.receiverMobile()
		);

		String orderNumber = generateOrderNumber();

		// 중복 조회
		orderRepository.findByOrderNumber(orderNumber)
			.ifPresent(o -> { throw new CustomException(ErrorCode.DUPLICATE_ORDER_NUMBER); });

		//주문 생성 (바로 주문)
		Order order = Order.create(userId, receiver, orderNumber, OrderType.CART);

		//주문 저장 전 동시성 제어(따닥 요청 방지, 동일한 주문번호의 주문이 저장되면 에러)
		try {
			orderRepository.save(order);
		}catch(DataIntegrityViolationException e){
			throw new CustomException(ErrorCode.DUPLICATE_ORDER_NUMBER);
		}

		// 주문 상품 생성
		OrderProduct orderProduct = OrderProduct.create(product, request.quantity(), order);
		orderProductRepository.save(orderProduct);
		order.mapToOrder(orderProduct);

		// 총 금액 계산
		order.calculateTotalAmount();

		// 배송 정보 생성
		createDeliveryForOrder(order.getId(), request.deliveryAddressId(), request.deliveryFee());

		//DTO에서 받은 결제 수단을 Enum으로 변환
		PaymentType type = PaymentType.valueOf(request.paymentType());

		//결제 정보 생성
		Payment payment = Payment.create(
			userId,
			order,
			request.deliveryFee().longValue(),
			type
		);
		paymentRepository.save(payment);

		return order;
	}

	/**
	 * 주문 완료 처리
	 * 1) 결제가 완료된 후 호출
	 * 2) 결제 상태(DONE) 확인 후 -> 주문 상태 변경 + 재고 차감
	 */
	public Order completeOrder(Long userId, String orderNumber) {
		// 1. 주문 조회
		Order order = orderRepository.findByOrderNumberAndUserId(orderNumber, userId)
			.orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

		// 2. 주문 상태 확인
		orderValidator.validateForCompletion(order);

		// 3. 결제 완료 여부 확인
		Payment payment = paymentRepository.findByOrderNumber(orderNumber)
			.orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));
		orderValidator.validatePaymentCompleted(payment);

		// 4. 주문 완료 처리
		order.complete();

		// 5. 재고 차감
		orderStockService.deductStock(order.getOrderProducts());

		// 6. 장바구니 비우기 (장바구니 주문의 경우)
		clearCartIfCartOrder(userId, order);

		return order;
	}

	//내 주문 목록 조회
	public List<Order> myOrderList(Long userId) {
		return orderRepository.findByUserId(userId);
	}

	//내 주문 상세 조회
	public Order myOrderInfo(String orderNumber, Long userId) {
		return orderRepository.findByOrderNumberAndUserId(orderNumber, userId)
			.orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));
	}

	//내 주문 취소(결제승인 전 주문 취소)
	public Order cancelOrder(Long userId, String orderNumber) {
		Order order = orderRepository.findByOrderNumberAndUserId(orderNumber, userId)
			.orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

		// 사용자는 PENDING 상태에서만 취소 가능
		order.cancel();

		// PENDING 상태 취소 = 결제 전 = 재고 차감 안 됨 = 재고복구x

		return order;
	}

	//결제 취소 구현

	//수정 = 배송정보 수정
	public Order updateOrder(Long userId, String orderNumber, OrderRequest.Update request) {

		Order order = orderRepository.findByOrderNumberAndUserId(orderNumber, userId)
			.orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

		var receiver = new Receiver(
			request.receiverName(),
			request.receiverAddress(),
			request.receiverMobile()
		);

		order.updateReceiver(receiver);

		return order;
	}

	private void createDeliveryForOrder(Long orderId, Long deliveryAddressId, Integer deliveryFee) {
		var deliveryRequest = new DeliveryRequest.Create(
			orderId,
			deliveryAddressId,
			deliveryFee
		);

		deliveryService.createDelivery(deliveryRequest);
	}

	//장바구니 주문이면 장바구니 비우기
	private void clearCartIfCartOrder(Long userId, Order order) {
		//장바구니 주문이 아니면 건너뛰기
		if (!order.isCartOrder()) {
			return;
		}
		cartRepository.findByUserId(userId).ifPresent(cart -> {
			cartProductRepository.deleteAllByCartId(cart.getId());
		});
	}
}