package com.kt.controller.product;

import com.kt.common.AbstractRestDocsTest;
import com.kt.common.RestDocsFactory;
import com.kt.common.api.ApiResponse;
import com.kt.domain.inventory.Inventory;
import com.kt.domain.pet.PetType;
import com.kt.domain.product.Product;
import com.kt.domain.tag.Tag;
import com.kt.dto.product.ProductTagRequest;
import com.kt.dto.product.ProductTagResponse;
import com.kt.repository.inventory.InventoryRepository;
import com.kt.repository.product.ProductRepository;
import com.kt.repository.product.ProductTagRepository;
import com.kt.repository.tag.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
public class AdminProductTagControllerTest extends AbstractRestDocsTest {

    private static final String DEFAULT_URL = "/admin/products";

    @Autowired
    private RestDocsFactory restDocsFactory;

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductTagRepository productTagRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private TagRepository tagRepository;

    private Product product;
    private Tag tag1;
    private Tag tag2;

    @BeforeEach
    void setUp() {
        productTagRepository.deleteAll();
        tagRepository.deleteAll();
        inventoryRepository.deleteAll();
        productRepository.deleteAll();

        product = createProduct("태그 매핑 상품", "설명", 10_000, PetType.DOG);
        tag1 = tagRepository.save(Tag.create("ALLERGY_HYPOALLERGENIC", "저알러지", PetType.DOG));
        tag2 = tagRepository.save(Tag.create("TREATS_BOTH", "간식", PetType.BOTH));
    }

    @Nested
    class 상품_태그_설정_API {

        @Test
        void 성공() throws Exception {
            // given
            ProductTagRequest request = new ProductTagRequest(List.of(tag1.getId(), tag2.getId()));

            var responseBody = new ProductTagResponse.ReplaceResult(
                    product.getId(),
                    List.of(
                            new ProductTagResponse.TagItem(tag1.getId(), tag1.getName()),
                            new ProductTagResponse.TagItem(tag2.getId(), tag2.getName())
                    )
            );
            var docsResponse = ApiResponse.of(responseBody);

            // when & then
            mockMvc.perform(
                            restDocsFactory.createRequest(
                                    DEFAULT_URL + "/{productId}/tags",
                                    request,
                                    HttpMethod.PUT,
                                    objectMapper,
                                    product.getId()
                            ).with(jwtAdmin())
                    )
                    .andExpect(status().isOk())
                    .andDo(
                            restDocsFactory.success(
                                    "admin-product-tags-replace",
                                    "상품 태그 설정",
                                    "관리자 상품 태그 설정(교체) API",
                                    "Admin-Product",
                                    request,
                                    docsResponse
                            )
                    );
        }
    }

    private Product createProduct(String name, String description, int price, PetType petType) {
        Product saved = productRepository.save(Product.create(name, description, price, petType));
        inventoryRepository.save(Inventory.initialize(saved));
        return saved;
    }
}