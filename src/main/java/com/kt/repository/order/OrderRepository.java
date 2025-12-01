package com.kt.repository.order;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kt.domain.order.Order;

public interface OrderRepository extends JpaRepository<Order,Long>{

	List<Order> findByUserId(Long userId);

	Optional<Order> findByOrderNumber(String orderNumber);

	Optional<Order> findByOrderNumberAndUserId(String orderNumber, Long userId);

}
