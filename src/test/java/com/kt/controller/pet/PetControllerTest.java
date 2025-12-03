package com.kt.controller.pet;

import com.kt.common.AbstractRestDocsTest;
import com.kt.common.RestDocsFactory;
import com.kt.domain.pet.BodyShape;
import com.kt.domain.pet.PetType;
import com.kt.domain.user.Gender;
import com.kt.domain.user.User;
import com.kt.dto.pet.PetRequest;
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

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@ActiveProfiles("test")
public class PetControllerTest extends AbstractRestDocsTest {

    private static final String PET_CREATE_URL = "/my/pets";

    private static final String LOGIN_ID = "loginUser123";
    private static final String PASSWORD = "PasswordTest123!";

    @Autowired
    private RestDocsFactory restDocsFactory;
    @Autowired
    private UserRepository userRepository;
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
                                    PET_CREATE_URL,
                                    request,
                                    HttpMethod.POST,
                                    objectMapper
                            ).with(jwtUser(currentUserId))   // ← 여기!
                    )
                    .andExpect(status().isCreated());
        }
    }

}
