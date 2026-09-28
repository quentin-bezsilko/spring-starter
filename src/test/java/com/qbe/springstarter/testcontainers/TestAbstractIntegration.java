package com.qbe.springstarter.testcontainers;

import com.qbe.springstarter.dto.SampleDto;
import com.qbe.springstarter.entity.SampleEntity;
import com.qbe.springstarter.enums.Status;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Base64;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"management.tracing.enabled=false"})
public abstract class TestAbstractIntegration {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.execute("TRUNCATE TABLE mydb.sample_entity RESTART IDENTITY CASCADE");
    }

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", Containers.POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", Containers.POSTGRES::getUsername);
        registry.add("spring.datasource.password", Containers.POSTGRES::getPassword);
        registry.add("spring.data.redis.host", Containers.REDIS::getHost);
        registry.add("spring.data.redis.port", () -> Containers.REDIS.getMappedPort(6379));
    }

    protected SampleDto buildSampleDto(String name) {
        return SampleDto.builder()
                .name(name)
                .description("Sample product description")
                .quantity(100)
                .stock(5000L)
                .weight(12.5)
                .ratio(0.75f)
                .price(BigDecimal.valueOf(99.99))
                .active(true)
                .category('A')
                .manufacturedDate(LocalDate.now())
                .manufacturedTime(LocalTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(Instant.now())
                .externalId(UUID.randomUUID())
                .document(Base64.getDecoder().decode("SGVsbG8gV29ybGQ="))
                .comments("This is a sample comment")
                .status(Status.CREATED)
                .build();
    }

    protected SampleEntity buildSampleEntity(String name) {
        SampleEntity entity = new SampleEntity();
        entity.setName(name);
        entity.setDescription("Sample product description");
        entity.setQuantity(100);
        entity.setStock(5000L);
        entity.setWeight(12.5);
        entity.setRatio(0.75f);
        entity.setPrice(BigDecimal.valueOf(99.99));
        entity.setActive(true);
        entity.setCategory('A');
        entity.setManufacturedDate(LocalDate.now());
        entity.setManufacturedTime(LocalTime.now());
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(Instant.now());
        entity.setExternalId(UUID.randomUUID());
        entity.setDocument(Base64.getDecoder().decode("SGVsbG8gV29ybGQ="));
        entity.setComments("This is a sample comment");
        entity.setStatus(Status.CREATED);

        return entity;
    }
}
