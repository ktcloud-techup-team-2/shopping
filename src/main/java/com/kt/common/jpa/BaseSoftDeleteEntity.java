package com.kt.common.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass
public abstract class BaseSoftDeleteEntity extends BaseAuditEntity {

	@Column(nullable = false)
	protected boolean deleted;

	protected LocalDateTime deletedAt;

	protected Long deletedBy;

	protected void markDeleted(Long deleterId) {
		this.deleted = true;
		this.deletedAt = LocalDateTime.now();
		this.deletedBy = deleterId;
	}
}