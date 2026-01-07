package com.kt.controller.tag;

import com.kt.common.AbstractRestDocsTest;
import com.kt.common.RestDocsFactory;
import com.kt.common.api.ApiResponse;
import com.kt.common.api.PageBlock;
import com.kt.domain.pet.PetType;
import com.kt.domain.tag.Tag;
import com.kt.dto.tag.TagRequest;
import com.kt.dto.tag.TagResponse;
import com.kt.repository.tag.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpMethod;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
public class TagControllerTest extends AbstractRestDocsTest {
    private static final String DEFAULT_URL = "/admin/tags";

    @Autowired
    private RestDocsFactory restDocsFactory;

    @Autowired
    private TagRepository tagRepository;

    @BeforeEach
    void setUp() {
        tagRepository.deleteAll();
    }

    @Nested
    class 태그_생성_API {

        @Test
        void 성공() throws Exception {
            // given
            TagRequest.Create request = new TagRequest.Create(
                    "ALLERGY_HYPOALLERGENIC",
                    "저알러지",
                    PetType.DOG
            );

            var responseBody = new TagResponse.Detail(
                    1L,
                    request.key(),
                    request.name(),
                    request.petType(),
                    true
            );
            var docsResponse = ApiResponse.of(responseBody);

            // when & then
            mockMvc.perform(
                            restDocsFactory.createRequest(
                                    DEFAULT_URL,
                                    request,
                                    HttpMethod.POST,
                                    objectMapper
                            ).with(jwtAdmin())
                    )
                    .andExpect(status().isCreated())
                    .andDo(
                            restDocsFactory.success(
                                    "admin-tags-create",
                                    "태그 생성",
                                    "관리자 태그 생성 API",
                                    "Admin-Tag",
                                    request,
                                    docsResponse
                            )
                    );
        }
    }

    @Nested
    class 태그_단건_조회_API {

        @Test
        void 성공() throws Exception {
            // given
            Tag saved = tagRepository.save(Tag.create("HEALTH_URINARY", "요로 건강", PetType.CAT));
            var docsResponse = ApiResponse.of(TagResponse.Detail.from(saved));

            // when & then
            mockMvc.perform(
                            restDocsFactory.createRequest(
                                    DEFAULT_URL + "/{tagId}",
                                    null,
                                    HttpMethod.GET,
                                    objectMapper,
                                    saved.getId()
                            ).with(jwtAdmin())
                    )
                    .andExpect(status().isOk())
                    .andDo(
                            restDocsFactory.success(
                                    "admin-tags-detail",
                                    "태그 상세 조회",
                                    "관리자 태그 단건 조회 API",
                                    "Admin-Tag",
                                    null,
                                    docsResponse
                            )
                    );
        }
    }

    @Nested
    class 태그_목록_조회_API {

        @Test
        void 성공() throws Exception {
            // given
            tagRepository.save(Tag.create("ALLERGY_HYPOALLERGENIC", "저알러지", PetType.DOG));
            tagRepository.save(Tag.create("HEALTH_URINARY", "요로 건강", PetType.CAT));

            PageRequest pageable = PageRequest.of(0, 10);
            Page<TagResponse.Detail> page = tagRepository
                    .findAllByDeletedFalse(pageable)
                    .map(TagResponse.Detail::from);

            var docsResponse = ApiResponse.ofPage(page.getContent(), toPageBlock(page));

            // when & then
            mockMvc.perform(
                            restDocsFactory.createParamRequest(
                                    DEFAULT_URL,
                                    null,
                                    pageable,
                                    objectMapper
                            ).with(jwtAdmin())
                    )
                    .andExpect(status().isOk())
                    .andDo(
                            restDocsFactory.successWithRequestParameters(
                                    "admin-tags-list",
                                    "태그 목록 조회",
                                    "관리자 태그 목록 조회 API",
                                    "Admin-Tag",
                                    null,
                                    pageable,
                                    objectMapper,
                                    docsResponse
                            )
                    );
        }
    }

    @Nested
    class 태그_수정_API {

        @Test
        void 성공() throws Exception {
            // given
            Tag saved = tagRepository.save(Tag.create("ALLERGY_HYPOALLERGENIC", "저알러지", PetType.DOG));

            TagRequest.Update request = new TagRequest.Update(
                    "ALLERGY_HYPOALLERGENIC",
                    "저알러지(수정)",
                    PetType.DOG,
                    true
            );

            var responseBody = new TagResponse.Detail(
                    saved.getId(),
                    request.key(),
                    request.name(),
                    request.petType(),
                    request.active()
            );
            var docsResponse = ApiResponse.of(responseBody);

            // when & then
            mockMvc.perform(
                            restDocsFactory.createRequest(
                                    DEFAULT_URL + "/{tagId}",
                                    request,
                                    HttpMethod.PATCH,
                                    objectMapper,
                                    saved.getId()
                            ).with(jwtAdmin())
                    )
                    .andExpect(status().isOk())
                    .andDo(
                            restDocsFactory.success(
                                    "admin-tags-update",
                                    "태그 수정",
                                    "관리자 태그 수정 API",
                                    "Admin-Tag",
                                    request,
                                    docsResponse
                            )
                    );
        }
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
