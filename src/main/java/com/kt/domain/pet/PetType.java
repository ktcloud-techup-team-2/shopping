package com.kt.domain.pet;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PetType {
	DOG("강아지"),
	CAT("고양이"),
    BOTH("공통");
	private final String description;
}