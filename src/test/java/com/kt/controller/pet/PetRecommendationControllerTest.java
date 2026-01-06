package com.kt.controller.pet;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.kt.common.AbstractRestDocsTest;
import com.kt.common.RestDocsFactory;
import com.kt.common.api.ApiResponse;
import com.kt.common.api.PageBlock;
import com.kt.domain.pet.BodyShape;
import com.kt.domain.pet.Pet;
import com.kt.domain.pet.PetType;
import com.kt.domain.product.Product;
import com.kt.domain.product.ProductTag;
import com.kt.domain.tag.Tag;
import com.kt.domain.user.Gender;
import com.kt.domain.user.User;
import com.kt.dto.pet.PetRecommendProductResponse;
import com.kt.repository.pet.PetRepository;
import com.kt.repository.product.ProductRepository;
import com.kt.repository.product.ProductTagRepository;
import com.kt.repository.tag.TagRepository;
import com.kt.repository.user.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.crypto.password.PasswordEncoder;

@Transactional
class PetRecommendationControllerTest extends AbstractRestDocsTest {

    private static final String BASE_URL = "/my/pets";

    private static final String LOGIN_ID = "recommendUser123";
    private static final String PASSWORD = "PasswordTest123!";

    @Autowired
    private RestDocsFactory restDocsFactory;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private ProductTagRepository productTagRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Long currentUserId;
    private Long petId;

    @BeforeEach
    void setUp() {
        productTagRepository.deleteAll();
        productRepository.deleteAll();
        petRepository.deleteAll();
        userRepository.deleteAll();

        User user = User.user(
                LOGIN_ID,
                passwordEncoder.encode(PASSWORD),
                "추천테스트유저",
                "recommend@example.com",
                "010-0000-0000",
                LocalDate.of(2000, 8, 9),
                Gender.FEMALE,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        currentUserId = userRepository.save(user).getId();

        Pet pet = Pet.create(
                user,
                PetType.DOG,
                "퍼피",
                Gender.MALE,
                false,
                "말티즈",
                LocalDate.now().minusMonths(8),
                4.1,
                BodyShape.CHUBBY,
                false,
                null
        );
        petId = petRepository.save(pet).getId();
    }

    @Nested
    class 펫_추천_태그_API {

        @Test
        void 성공() throws Exception {
            var docsResponse = ApiResponse.of(
                    Map.of(
                            "tags", List.of(
                                    Map.of("id", 22, "key", "DOG_LIFE_STAGE_PUPPY", "name", "강아지 퍼피", "petType", "DOG"),
                                    Map.of("id", 28, "key", "DOG_FOOD", "name", "강아지 사료", "petType", "DOG")
                            )
                    )
            );

            mockMvc.perform(
                            restDocsFactory.createRequest(
                                    BASE_URL + "/{petId}/recommend-tags",
                                    null,
                                    HttpMethod.GET,
                                    objectMapper,
                                    petId
                            ).with(jwtUser(currentUserId))
                    )
                    .andExpect(status().isOk())
                    .andDo(
                            restDocsFactory.success(
                                    "pet-recommend-tags",
                                    "펫 추천 태그 조회",
                                    "펫 정보 기반 추천 태그 조회 API",
                                    "Pet",
                                    null,
                                    docsResponse
                            )
                    );
        }
    }

    @Nested
    class 펫_추천_상품_API {

        @Test
        void 성공() throws Exception {
            Tag diet = getOrCreateTag("DIET_WEIGHT_CONTROL", "체중 관리(다이어트)", PetType.BOTH);
            Tag puppy = getOrCreateTag("DOG_LIFE_STAGE_PUPPY", "강아지 퍼피", PetType.DOG);


            Product puppyFood = createActiveProduct("퍼피 전용 프리미엄 사료", "퍼피 전용 사료", 32000, PetType.DOG);
            Product dogFoodProduct = createActiveProduct("강아지 사료 닭고기맛 1kg", "일반 사료", 18000, PetType.DOG);

            productTagRepository.saveAll(List.of(
                    ProductTag.create(puppyFood, diet),
                    ProductTag.create(puppyFood, puppy),
                    ProductTag.create(dogFoodProduct, diet)
            ));

            PageRequest pageable = PageRequest.of(0, 10);

            var items = List.of(
                    new PetRecommendProductResponse.ProductItem(
                            puppyFood.getId(),
                            puppyFood.getName(),
                            puppyFood.getPrice(),
                            "ACTIVE",
                            PetType.DOG,
                            2L
                    ),
                    new PetRecommendProductResponse.ProductItem(
                            dogFoodProduct.getId(),
                            dogFoodProduct.getName(),
                            dogFoodProduct.getPrice(),
                            "ACTIVE",
                            PetType.DOG,
                            1L
                    )
            );

            Page<PetRecommendProductResponse.ProductItem> page = new PageImpl<>(items, pageable, items.size());
            var docsResponse = ApiResponse.ofPage(items, toPageBlock(page));

            mockMvc.perform(
                            restDocsFactory.createParamRequest(
                                    BASE_URL + "/{petId}/recommend-products",
                                    null,
                                    pageable,
                                    objectMapper,
                                    petId
                            ).with(jwtUser(currentUserId))
                    )
                    .andExpect(status().isOk())
                    .andDo(
                            restDocsFactory.successWithRequestParameters(
                                    "pet-recommend-products",
                                    "펫 추천 상품 조회",
                                    "펫 정보 기반 추천 상품 리스트 조회 API",
                                    "Pet",
                                    null,
                                    pageable,
                                    objectMapper,
                                    docsResponse
                            )
                    );
        }
    }

    private Tag getOrCreateTag(String key, String name, PetType petType) {
        List<Tag> tags = tagRepository.findAllByKeyInAndActiveTrueAndDeletedFalse(Set.of(key));
        if (!tags.isEmpty()) return tags.getFirst();

        Tag newTag = Tag.create(key, name, petType);
        return tagRepository.save(newTag);
    }

    private Product createActiveProduct(String name, String description, int price, PetType petType) {
        Product product = productRepository.save(Product.create(name, description, price, petType));
        product.activate();
        return productRepository.save(product);
    }

    private PageBlock toPageBlock(Page<?> page) {
        return new PageBlock(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext(),
                page.hasPrevious(),
                page.getSort().stream()
                        .map(order -> new PageBlock.SortOrder(order.getProperty(), order.getDirection().name()))
                        .toList()
        );
    }
}
