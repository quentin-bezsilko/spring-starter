package com.qbe.springstarter.config;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public RedisCacheManager cacheManager(
            RedisConnectionFactory connectionFactory, @Value("${cache.ttl.samples}") Duration samplesTtl) {

        RedisCacheConfiguration samplesConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(samplesTtl)
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        new JdkSerializationRedisSerializer()));

        return RedisCacheManager.builder(connectionFactory)
                .withCacheConfiguration("samples", samplesConfig)
                .build();
    }
}
