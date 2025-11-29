package com.kt.domain.order;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class Receiver {

	@Column(name = "receiver_name")
	private String name;

	@Column(name = "receiver_address")
	private String address;

	@Column(name = "reciver_moblie")
	private String mobile;

}
