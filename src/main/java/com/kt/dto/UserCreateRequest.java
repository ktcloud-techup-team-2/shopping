package com.kt.dto;

import java.time.LocalDate;

public record UserCreateRequest(
	String loginId,
	String password,
	String name,
	LocalDate birthday
) {

}
