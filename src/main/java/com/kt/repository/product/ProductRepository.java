package com.kt.repository.product;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.kt.domain.product.Product;
import com.kt.domain.product.ProductStatus;

public interface ProductRepository extends JpaRepository<Product, Long>, ProductRepositoryCustom {
	Optional<Product> findByIdAndDeletedFalse(Long id);
	List<Product> findByIdInAndDeletedFalse(Collection<Long> ids);
}
