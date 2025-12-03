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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@ActiveProfiles("test")
public class PetControllerTest extends AbstractRestDocsTest {

    private static final String PET_URL = "/my/pets";

    private static final String LOGIN_ID = "loginUser123";
    private static final String PASSWORD = "PasswordTest123!";

    @Autowired
    private RestDocsFactory restDocsFactory;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PetRepository petRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private Long currentUserId;

    @BeforeEach
    void setUpUser() {
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
            User user = userRepository.findById(currentUserId).orElseThrow();

            Pet pet = Pet.create(
                    user,
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
            Long petId = petRepository.save(pet).getId();

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
                            petId
                    ).with(jwtUser(currentUserId))
            )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(petId))
                    .andExpect(jsonPath("$.data.weight").value(request.weight()))
                    .andExpect(jsonPath("$.data.neutered").value(request.neutered()))
                    .andExpect(jsonPath("$.data.bodyShape").value(request.bodyShape().name()))
                    .andExpect(jsonPath("$.data.allergy").value(request.allergy()));
        }
    }
}
