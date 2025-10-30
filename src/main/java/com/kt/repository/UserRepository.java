package com.kt.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.kt.domain.User;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserRepository {
	private final JdbcTemplate jdbcTemplate;
	public void save(User user) {
		String sql = """
  			INSERT INTO MEMBER (loginId, password, name, birthday)
  			VALUES (?, ?, ?, ?);
		""";
		jdbcTemplate.update(sql,
			user.getLoginId(), user.getPassword(), user.getName(), user.getBirthday());
	}
}
