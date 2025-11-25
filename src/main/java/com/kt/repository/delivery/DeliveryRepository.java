package com.kt.repository.delivery;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kt.domain.delivery.Delivery;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

	Optional<Delivery> findByOrderId(Long orderId);

	Optional<Delivery> findByTrackingNumber(String trackingNumber);
}
