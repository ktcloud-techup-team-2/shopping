package com.kt.service.order;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kt.common.api.CustomException;
import com.kt.common.api.ErrorCode;
import com.kt.domain.cart.Cart;
import com.kt.domain.cartproduct.CartProduct;
import com.kt.domain.inventory.Inventory;
import com.kt.domain.order.Order;
import com.kt.domain.order.Receiver;
import com.kt.domain.orderproduct.OrderProduct;
import com.kt.domain.product.Product;
import com.kt.dto.delivery.DeliveryRequest;
import com.kt.dto.order.OrderRequest;
import com.kt.repository.cart.CartProductRepository;
import com.kt.repository.cart.CartRepository;
import com.kt.repository.inventory.InventoryRepository;
import com.kt.repository.order.OrderRepository;
import com.kt.repository.orderproduct.OrderProductRepository;
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
	private final InventoryRepository inventoryRepository;

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

		if (cartProducts.isEmpty()) {
			throw new CustomException(ErrorCode.CART_EMPTY);
		}

		Receiver receiver = new Receiver(
			request.receiverName(),
			request.receiverAddress(),
			request.receiverMobile()
		);

		//주문 생성
		String orderNumber = generateOrderNumber();
		Order order = Order.create(userId, receiver, orderNumber);
		orderRepository.save(order);

		// 재고 검증 및 주문 상품 생성
		for (CartProduct cartProduct : cartProducts) {
			Product product = cartProduct.getProduct();
			int quantity = cartProduct.getCount();

			// 재고 검증
			validateStock(product.getId(), quantity);

			// 주문 상품 생성
			OrderProduct orderProduct = OrderProduct.create(product, quantity, order);
			orderProductRepository.save(orderProduct);
			order.mapToOrder(orderProduct);
		}
		//총 금액 계산
		order.calculateTotalAmount();

		//TODO: 주문 완료 시점이 아닌 '결제 완료' 시점에 장바구니를 비우도록 변경하기
		cartProductRepository.deleteAllByCartId(cart.getId());

		// 배송 정보 생성
		createDeliveryForOrder(order.getId(), request.deliveryAddressId(), request.deliveryFee());

		return order;
	}

	// 2. 바로 주문
	public Order createDirectOrder(Long userId, OrderRequest.DirectOrder request) {
		// 상품 조회
		Product product = productRepository.findById(request.productId())
			.orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

		// 재고 검증
		validateStock(product.getId(), request.quantity());

		Receiver receiver = new Receiver(
			request.receiverName(),
			request.receiverAddress(),
			request.receiverMobile()
		);

		// 주문 생성
		String orderNumber = generateOrderNumber();
		Order order = Order.create(userId, receiver, orderNumber);
		orderRepository.save(order);

		// 주문 상품 생성
		OrderProduct orderProduct = OrderProduct.create(product, request.quantity(), order);
		orderProductRepository.save(orderProduct);
		order.mapToOrder(orderProduct);

		// 총 금액 계산
		order.calculateTotalAmount();

		// 배송 정보 생성
		createDeliveryForOrder(order.getId(), request.deliveryAddressId(), request.deliveryFee());

		return order;
	}

	// 재고 검증
	private void validateStock(Long productId, int requestedQuantity) {
		Inventory inventory = inventoryRepository.findByProductId(productId)
			.orElseThrow(() -> new CustomException(ErrorCode.INVENTORY_NOT_FOUND));

		// 재고가 1 미만이면 주문 불가
		if (inventory.getAvailable() < 1) {
			throw new CustomException(ErrorCode.PRODUCT_STOCK_NOT_ENOUGH);
		}

		//TODO : 가용재고랑 실재고 중 어느걸 바탕으로 검증해야 하는지 고민해보기

		// 요청 수량이 가용 재고보다 많으면 주문 불가
		if (requestedQuantity > inventory.getAvailable()) {
			throw new CustomException(ErrorCode.PRODUCT_STOCK_NOT_ENOUGH);
		}
	}

	public List<Order> myOrderList(Long userId) {
		return orderRepository.findByUserId(userId);
	}

	public Order myOrderInfo(String orderNumber, Long userId) {
		return orderRepository.findByOrderNumberAndUserId(orderNumber, userId)
			.orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));
	}

	public Order cancelOrder(Long userId, String orderNumber) {
		Order order = orderRepository.findByOrderNumberAndUserId(orderNumber, userId)
			.orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

		order.cancel();

		return order;
	}

	//수정
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
}