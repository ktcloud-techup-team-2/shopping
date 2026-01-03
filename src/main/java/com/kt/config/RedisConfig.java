package com.kt.config;

import java.time.Duration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;

@Configuration
@ConditionalOnProperty(value = "app.redis.enabled", havingValue = "true", matchIfMissing = true)
public class RedisConfig {

    @Bean
    @Profile({"dev", "prod"})
    public RedisConnectionFactory clusterRedisConnectionFactory(RedisProperties redisProperties) {
        RedisProperties.Cluster cluster = redisProperties.getCluster();
        if (cluster == null || cluster.getNodes() == null || cluster.getNodes().isEmpty()) {
            throw new IllegalStateException("dev/prod 환경에서는 Redis 클러스터 설정이 필요합니다.");
        }

        RedisClusterConfiguration clusterConfig = new RedisClusterConfiguration(cluster.getNodes());
        if (cluster.getMaxRedirects() != null) {
            clusterConfig.setMaxRedirects(cluster.getMaxRedirects());
        }
        if (StringUtils.hasText(redisProperties.getPassword())) {
            clusterConfig.setPassword(RedisPassword.of(redisProperties.getPassword()));
        }

        return new LettuceConnectionFactory(clusterConfig, buildClientConfig(redisProperties));
    }

    @Bean
    @Profile({"default", "local", "test"})
    public RedisConnectionFactory standaloneRedisConnectionFactory(RedisProperties redisProperties) {
        RedisStandaloneConfiguration standaloneConfig = new RedisStandaloneConfiguration(
                redisProperties.getHost(),
                redisProperties.getPort()
        );
        if (StringUtils.hasText(redisProperties.getPassword())) {
            standaloneConfig.setPassword(RedisPassword.of(redisProperties.getPassword()));
        }

        return new LettuceConnectionFactory(standaloneConfig, buildClientConfig(redisProperties));
    }

    private LettuceClientConfiguration buildClientConfig(RedisProperties redisProperties) {
        LettuceClientConfiguration.LettuceClientConfigurationBuilder builder = LettuceClientConfiguration.builder();
        if (redisProperties.getSsl() != null && redisProperties.getSsl().isEnabled()) {
            builder.useSsl();
        }
        Duration timeout = redisProperties.getTimeout();
        if (timeout != null) {
            builder.commandTimeout(timeout);
        }
        return builder.build();
    }

    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }
}
