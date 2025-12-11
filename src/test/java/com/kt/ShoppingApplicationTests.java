package com.kt;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.kt.config.TestRedisConfig;

@SpringBootTest
@ActiveProfiles("test")
@Import({TestRedisConfig.class})
class ShoppingApplicationTests {

	@Test
	void contextLoads() {
	}

}
