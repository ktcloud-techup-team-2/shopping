package com.kt.repository.inventory;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kt.domain.inventory.ProcessedInboundEvent;

public interface ProcessedInboundEventRepository extends JpaRepository<ProcessedInboundEvent, Long> {
}