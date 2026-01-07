package com.kt.repository.product;

import com.kt.domain.pet.PetType;
import com.kt.dto.product.ProductRecommendRow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductTagRepositoryCustom {
    Page<ProductRecommendRow> findRecommendedProductsByTagIds(List<Long> tagIds, PetType petType, Pageable pageable);
}
