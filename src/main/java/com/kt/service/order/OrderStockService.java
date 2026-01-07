package com.kt.service.order;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kt.domain.orderproduct.OrderProduct;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderStockService {

	//결제 완료되면 재고 차감
	public void deductStock(List<OrderProduct> orderProducts) {
		for (OrderProduct orderProduct : orderProducts) {
			//재고차감 //동시성 제어
		}
	}

	//재고복구
	public void restoreStock(List<OrderProduct> orderProducts) {
		for (OrderProduct orderProduct : orderProducts) {
		}
	}
}

