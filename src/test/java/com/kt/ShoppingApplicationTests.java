package com.kt;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import com.kt.config.TestRedisConfig;

@SpringBootTest
@Import({TestRedisConfig.class})
class ShoppingApplicationTests {

	@Test
	void contextLoads() {
	}

}
