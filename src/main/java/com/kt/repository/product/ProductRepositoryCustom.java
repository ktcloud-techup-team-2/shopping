package com.kt.repository.product;

import com.kt.domain.product.Product;
import com.kt.domain.product.ProductStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductRepositoryCustom {

	Page<Product> findNonDeleted(Pageable pageable);

	Page<Product> findNonDeletedByStatuses(Collection<ProductStatus> statuses, Pageable pageable);

	Optional<Product> findNonDeletedById(Long id);

	Optional<Product> findNonDeletedByIdAndStatuses(Long id, Collection<ProductStatus> statuses);

	List<Product> findAllForUpdateByIds(Collection<Long> ids);
}