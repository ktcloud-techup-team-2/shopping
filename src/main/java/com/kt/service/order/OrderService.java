package com.kt.service.order;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kt.common.api.CustomException;
import com.kt.common.api.ErrorCode;
import com.kt.domain.order.Order;
import com.kt.domain.order.Receiver;
import com.kt.domain.orderproduct.OrderProduct;
import com.kt.dto.delivery.DeliveryRequest;
import com.kt.dto.order.OrderRequest;
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

	//주문번호 생성
	private String generateOrderNumber(){
		String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern(("yyyyMMdd")));

		String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		Random random = new Random();
		StringBuilder randomNumber = new StringBuilder(5);
		for(int i = 0; i < 5; i++){
			randomNumber.append(chars.charAt(random.nextInt(chars.length())));
		}
		return "ORD-"+date+randomNumber;
	}

	//주문 생성
	public Order createOrder(Long userId, OrderRequest.Create request){

		var product = productRepository.findById(request.productId())
			.orElseThrow(()-> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

		var calculatePaymentAmount = (long)product.getPrice()*request.quantity();

		var receiver = new Receiver(
			request.receiverName(),
			request.receiverAddress(),
			request.receiverMobile()
		);

		var orderNumber = generateOrderNumber();
		var order = Order.create(userId, receiver, calculatePaymentAmount, orderNumber);
		var orderProduct = OrderProduct.create(product, request.quantity(), order);

		order.mapToOrder(orderProduct);

		orderProductRepository.save(orderProduct);

		var savedOrder = orderRepository.save(order);
		createDeliveryForOrder(savedOrder.getId(), request.deliveryAddressId(), request.deliveryFee());

		return savedOrder;
	}

	public List<Order> myOrderList(Long userId){
		return orderRepository.findByUserId(userId);
	}

	public Order myOrderInfo(String orderNumber, Long userId){
		return orderRepository.findByOrderNumberAndUserId(orderNumber, userId)
			.orElseThrow(() -> new CustomException((ErrorCode.ORDER_NOT_FOUND)));
	}

	public Order cancelOrder(Long userId, String orderNumber){
		Order order = orderRepository.findByOrderNumberAndUserId(orderNumber, userId)
			.orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

		order.cancel();

		return order;
	}

	//수정
	public Order updateOrder(Long userId, String orderNumber, OrderRequest.Update request){

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

	//order-product 연관관계

	private void createDeliveryForOrder(Long orderId, Long deliveryAddressId, Integer deliveryFee) {
		var deliveryRequest = new DeliveryRequest.Create(
			orderId,
			deliveryAddressId,
			deliveryFee
		);

		deliveryService.createDelivery(deliveryRequest);

	}
}