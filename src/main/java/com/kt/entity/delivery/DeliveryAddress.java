package com.kt.entity.delivery;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.kt.dto.delivery.DeliveryAddressRequest;

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
	private LocalDateTime createdAt;

	@LastModifiedDate
	private LocalDateTime updatedAt;

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

	public static DeliveryAddress from(Long userId, DeliveryAddressRequest.Create request) {
		DeliveryAddress address = new DeliveryAddress();
		address.userId = userId;
		address.addressName = request.addressName();
		address.receiverName = request.receiverName();
		address.receiverMobile = request.receiverMobile();
		address.postalCode = request.postalCode();
		address.roadAddress = request.roadAddress();
		address.detailAddress = request.detailAddress();
		address.isDefault = request.isDefault() != null ? request.isDefault() : false;
		address.isActive = true;
		return address;
	}

	public void update(String addressName, String receiverName, String receiverMobile,
		String postalCode, String roadAddress, String detailAddress) {
		this.addressName = addressName;
		this.receiverName = receiverName;
		this.receiverMobile = receiverMobile;
		this.postalCode = postalCode;
		this.roadAddress = roadAddress;
		this.detailAddress = detailAddress;
	}
}
