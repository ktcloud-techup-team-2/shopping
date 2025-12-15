package com.kt.repository.category;

import com.kt.domain.category.Category;
import com.kt.domain.category.CategoryStatus;
import com.kt.domain.pet.PetType;
import java.util.List;

public interface CategoryRepositoryCustom {

	List<Category> findAllForAdmin(PetType petType);

	List<Category> findAllByStatusAndPetType(CategoryStatus status, PetType petType);
}