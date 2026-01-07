package com.kt.repository.product;

import com.kt.domain.product.ProductTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductTagRepository extends JpaRepository<ProductTag, Long>, ProductTagRepositoryCustom {
    void deleteAllByProduct_Id(Long productId);
}
