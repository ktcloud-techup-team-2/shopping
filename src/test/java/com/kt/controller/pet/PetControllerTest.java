package com.kt.controller.pet;

import com.kt.common.AbstractRestDocsTest;
import com.kt.common.RestDocsFactory;
import com.kt.domain.pet.BodyShape;
import com.kt.domain.pet.Pet;
import com.kt.domain.pet.PetType;
import com.kt.domain.user.Gender;
import com.kt.domain.user.User;
import com.kt.dto.pet.PetRequest;
import com.kt.repository.pet.PetRepository;
import com.kt.repository.user.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@ActiveProfiles("test")
public class PetControllerTest extends AbstractRestDocsTest {

    private static final String PET_URL = "/my/pets";

    private static final String LOGIN_ID = "loginUser123";
    private static final String PASSWORD = "PasswordTest123!";

    @MockitoBean
    private StringRedisTemplate stringRedisTemplate;

    @MockitoBean
    private RedissonClient redissonClient;

    @Autowired
    private RestDocsFactory restDocsFactory;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PetRepository petRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private Long currentUserId;
    private Long defaultPetId;

    @BeforeEach
    void setUp() {
        petRepository.deleteAll();
        userRepository.deleteAll();

        User user = User.user(
                LOGIN_ID,
                passwordEncoder.encode(PASSWORD),
                "테스트유저",
                "example123@gmail.com",
                "010-1234-5678",
                LocalDate.of(2000, 8, 9),
                Gender.FEMALE,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        currentUserId = userRepository.save(user).getId();

        Pet pet = Pet.create(
                user,
                PetType.CAT,
                "TESTCAT",
                Gender.MALE,
                true,
                "스핑크스",
                "2012-03-01",
                4.5,
                BodyShape.SKINNY,
                true,
                "https://example.com/images/pobi.jpg"
        );

        defaultPetId = petRepository.save(pet).getId();
    }

    @Nested
    class 펫_등록_API {
        @Test
        void 성공() throws Exception {
            PetRequest.Create request = new PetRequest.Create(
                    PetType.DOG,
                    "포비",
                    Gender.MALE,
                    true,
                    "포메라니안",
                    "2012-03-01",
                    3.9,
                    BodyShape.SKINNY,
                    true,
                    "https://example.com/images/pobi.jpg"
            );

            mockMvc.perform(
                            restDocsFactory.createRequest(
                                    PET_URL,
                                    request,
                                    HttpMethod.POST,
                                    objectMapper
                            ).with(jwtUser(currentUserId))
                    )
                    .andExpect(status().isCreated());
        }
    }

    @Nested
    class 펫_수정_api {
        @Test
        void 성공()  throws Exception {

            PetRequest.Update request = new PetRequest.Update(
                    true,
                    4.5,
                    BodyShape.NORMAL,
                    true,
                    "https://example.com/images/pobi-updated.jpg"
            );

            mockMvc.perform(
                    restDocsFactory.createRequest(
                            PET_URL + "/{id}",
                            request,
                            HttpMethod.PATCH,
                            objectMapper,
                            defaultPetId
                    ).with(jwtUser(currentUserId))
            )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(defaultPetId))
                    .andExpect(jsonPath("$.data.weight").value(request.weight()))
                    .andExpect(jsonPath("$.data.neutered").value(request.neutered()))
                    .andExpect(jsonPath("$.data.bodyShape").value(request.bodyShape().name()))
                    .andExpect(jsonPath("$.data.allergy").value(request.allergy()));
        }
    }

    @Nested
    class 펫_단건_조회_API {
        @Test
        void 성공() throws Exception {
            mockMvc.perform(
                            restDocsFactory.createRequest(
                                    PET_URL + "/{id}",
                                    null,
                                    HttpMethod.GET,
                                    objectMapper,
                                    defaultPetId
                            ).with(jwtUser(currentUserId))
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(defaultPetId))
                    .andExpect(jsonPath("$.data.name").value("TESTCAT"));
        }
    }

    @Nested
    class 펫_목록_조회_API {

        @Test
        void 성공() throws Exception {
            User user = userRepository.findById(currentUserId).orElseThrow();

            // 추가 펫 생성
            Pet extra = Pet.create(
                    user,
                    PetType.CAT,
                    "나비",
                    Gender.FEMALE,
                    false,
                    "코리안숏헤어",
                    "2020-05-10",
                    4.5,
                    BodyShape.NORMAL,
                    false,
                    "https://example.com/images/nabi.jpg"
            );
            petRepository.save(extra);

            mockMvc.perform(
                            restDocsFactory.createRequest(
                                    PET_URL,
                                    null,
                                    HttpMethod.GET,
                                    objectMapper
                            ).with(jwtUser(currentUserId))
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(2));
        }
    }

    @Nested
    class 펫_삭제_API {
        @Test
        void 성공() throws Exception {
            mockMvc.perform(
                    restDocsFactory.createRequest(
                            PET_URL+"/{id}",
                            null,
                            HttpMethod.DELETE,
                            objectMapper,
                            defaultPetId
                    ).with(jwtUser(currentUserId))
            )
                    .andExpect(status().isNoContent());

            assertThat(petRepository.findByIdAndDeletedAtIsNull(defaultPetId)).isEmpty();

            Pet deletedPet = petRepository.findById(defaultPetId).orElseThrow();
            assertThat(deletedPet.isDeleted()).isTrue();
        }
    }
}
