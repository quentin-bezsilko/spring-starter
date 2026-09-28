package com.qbe.springstarter.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

class TestCacheConfig {

    @Test
    void shouldCreateRedisCacheManager() {
        CacheConfig cacheConfig = new CacheConfig();

        RedisConnectionFactory connectionFactory = mock(RedisConnectionFactory.class);
        RedisCacheManager cacheManager = cacheConfig.cacheManager(connectionFactory, Duration.ofMinutes(60));

        assertThat(cacheManager).isNotNull();
    }

    @Test
    void shouldCreateSamplesCache() {
        CacheConfig cacheConfig = new CacheConfig();
        RedisConnectionFactory connectionFactory = mock(RedisConnectionFactory.class);

        RedisCacheManager cacheManager = cacheConfig.cacheManager(connectionFactory, Duration.ofMinutes(60));
        Cache cache = cacheManager.getCache("samples");

        assertThat(cache).isNotNull();
        assertThat(cache.getName()).isEqualTo("samples");
    }
}
