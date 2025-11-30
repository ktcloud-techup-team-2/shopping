package com.kt.service.order;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kt.common.api.CustomException;
import com.kt.common.api.ErrorCode;
import com.kt.domain.order.Order;
import com.kt.domain.order.Receiver;
import com.kt.domain.orderproduct.OrderProduct;
import com.kt.dto.order.OrderRequest;
import com.kt.dto.order.OrderResponse;
import com.kt.repository.order.OrderRepository;
import com.kt.repository.orderproduct.OrderProductRepository;
import com.kt.repository.product.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {

	private final OrderRepository orderRepository;
	private final ProductRepository productRepository;
	private final OrderProductRepository orderProductRepository;

	public OrderResponse.Create createOrder(Long userId, OrderRequest.Create request){

		var product = productRepository.findById(request.productId())
			.orElseThrow(()-> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

		var calculatePaymentAmount = (long)product.getPrice()*request.quantity();

		var receiver = new Receiver(
			request.receiverName(),
			request.receiverAddress(),
			request.receiverMobile()
		);

		var order = Order.create(userId,receiver,calculatePaymentAmount);
		var orderProduct = OrderProduct.create(product, request.quantity(), order);

		orderProductRepository.save(orderProduct);
		return OrderResponse.Create.from(orderRepository.save(order));

	}
}
