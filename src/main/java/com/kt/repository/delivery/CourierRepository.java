package com.kt.repository.delivery;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kt.domain.delivery.Courier;

public interface CourierRepository extends JpaRepository<Courier, Long> {
	boolean existsByCode(String code);

	Optional<Courier> findByCode(String code);
}
