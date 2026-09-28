package com.qbe.springstarter.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;

class TestValidationErrorResponseDto {

    @Test
    void shouldCreateDto() {
        Instant timestamp = Instant.now();

        Map<String, String> errors = Map.of(
                "name", "Name is mandatory",
                "price", "Price is mandatory");

        ValidationErrorResponseDto dto = new ValidationErrorResponseDto(timestamp, 400, "Validation failed", errors);

        assertThat(dto.timestamp()).isEqualTo(timestamp);
        assertThat(dto.status()).isEqualTo(400);
        assertThat(dto.message()).isEqualTo("Validation failed");
        assertThat(dto.errors()).isEqualTo(errors);
    }

    @Test
    void shouldSupportEqualsAndHashCode() {
        Instant timestamp = Instant.now();

        ValidationErrorResponseDto dto1 =
                new ValidationErrorResponseDto(timestamp, 400, "Validation failed", Map.of("name", "mandatory"));

        ValidationErrorResponseDto dto2 =
                new ValidationErrorResponseDto(timestamp, 400, "Validation failed", Map.of("name", "mandatory"));

        assertThat(dto1).isEqualTo(dto2).hasSameHashCodeAs(dto2);
    }

    @Test
    void shouldGenerateToString() {
        ValidationErrorResponseDto dto = new ValidationErrorResponseDto(
                Instant.parse("2026-08-19T07:00:00Z"), 400, "Validation failed", Map.of("name", "mandatory"));

        assertThat(dto.toString())
                .contains("ValidationErrorResponseDto")
                .contains("Validation failed")
                .contains("400");
    }

    @Test
    void shouldNotBeEqualWhenValuesDiffer() {
        ValidationErrorResponseDto dto1 =
                new ValidationErrorResponseDto(Instant.now(), 400, "Validation failed", Map.of());

        ValidationErrorResponseDto dto2 =
                new ValidationErrorResponseDto(Instant.now(), 500, "Technical error", Map.of());

        assertThat(dto1).isNotEqualTo(dto2);
    }
}
