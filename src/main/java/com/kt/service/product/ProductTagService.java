package com.kt.service.product;

import com.kt.common.Preconditions;
import com.kt.common.api.CustomException;
import com.kt.common.api.ErrorCode;
import com.kt.domain.product.Product;
import com.kt.domain.product.ProductTag;
import com.kt.domain.tag.Tag;
import com.kt.dto.product.ProductTagRequest;
import com.kt.dto.product.ProductTagResponse;
import com.kt.repository.product.ProductRepository;
import com.kt.repository.product.ProductTagRepository;
import com.kt.repository.tag.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductTagService {

    private final ProductRepository productRepository;
    private final ProductTagRepository productTagRepository;
    private final TagRepository tagRepository;

    public ProductTagResponse.ReplaceResult replaceTags (Long productId, ProductTagRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

        List<Tag> tags = tagRepository.findAllById(request.tagIds());

        for (Tag tag : tags) {
            Preconditions.validate(tag.isActive(), ErrorCode.TAG_INACTIVE);
        }

        productTagRepository.deleteAllByProduct_Id(productId);
        productTagRepository.flush();

        List<ProductTag> mappings = tags.stream()
                .map(tag -> ProductTag.create(product, tag))
                .toList();
        productTagRepository.saveAll(mappings);

        List<ProductTagResponse.TagItem> tagItems = tags.stream()
                .map(tag -> new ProductTagResponse.TagItem(tag.getId(), tag.getName()))
                .toList();

        return new ProductTagResponse.ReplaceResult(productId, tagItems);
    }
}
