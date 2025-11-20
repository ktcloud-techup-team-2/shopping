package com.kt.entity.delivery;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(indexes = @Index(columnList = "userId"))
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class DeliveryAddress {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Long userId;

	private String addressName;
	private String receiverName;
	private String receiverMobile;
	private String postalCode;
	private String roadAddress;
	private String detailAddress;

	private Boolean isDefault = false;
	private Boolean isActive = true;

	@CreatedDate
	@Column(updatable = false)
	private LocalDateTime createAt;

	@LastModifiedDate
	private LocalDateTime updateAt;

	public void setAsDefault() {
		this.isDefault = true;
	}
	public void unsetAsDefault() {
		this.isDefault = false;
	}
	public void deactivate() {
		this.isActive = false;
	}
	public boolean isOwnedBy(Long userId) {
		return this.userId.equals(userId);
	}
}
