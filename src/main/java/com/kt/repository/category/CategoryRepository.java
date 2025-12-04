package com.kt.repository.category;

import com.kt.domain.category.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long>, CategoryRepositoryCustom {

	Optional<Category> findByIdAndDeletedFalse(Long id);
	List<Category> findAllByIdInAndDeletedFalse(Collection<Long> ids);
}