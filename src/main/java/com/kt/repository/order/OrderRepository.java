package com.kt.repository.order;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kt.domain.order.Order;

public interface OrderRepository extends JpaRepository<Order,Long>{

	// orderNumber로 주문 조회
	Optional<Order> findByOrderNumber(String orderNumber);

}
