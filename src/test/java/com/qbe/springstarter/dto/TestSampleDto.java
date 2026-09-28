package com.qbe.springstarter.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.qbe.springstarter.enums.Status;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestSampleDto {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldValidateValidDto() {
        SampleDto dto = buildValidDto();
        Set<ConstraintViolation<SampleDto>> violations = validator.validate(dto);
        assertThat(violations).isEmpty();
    }

    @Test
    void shouldFailWhenNameIsBlank() {
        SampleDto dto = buildValidDto().toBuilder().name("").build();

        Set<ConstraintViolation<SampleDto>> violations = validator.validate(dto);

        assertThat(violations).extracting(ConstraintViolation::getMessage).contains("Name is mandatory");
    }

    @Test
    void shouldFailWhenQuantityIsNull() {
        SampleDto dto = buildValidDto().toBuilder().quantity(null).build();

        Set<ConstraintViolation<SampleDto>> violations = validator.validate(dto);

        assertThat(violations).extracting(ConstraintViolation::getMessage).contains("Quantity is mandatory");
    }

    @Test
    void shouldFailWhenQuantityIsNegative() {
        SampleDto dto = buildValidDto().toBuilder().quantity(-1).build();

        Set<ConstraintViolation<SampleDto>> violations = validator.validate(dto);

        assertThat(violations).extracting(ConstraintViolation::getMessage).contains("Quantity must be greater than 0");
    }

    @Test
    void shouldFailWhenPriceIsNull() {
        SampleDto dto = buildValidDto().toBuilder().price(null).build();

        Set<ConstraintViolation<SampleDto>> violations = validator.validate(dto);

        assertThat(violations).extracting(ConstraintViolation::getMessage).contains("Price is mandatory");
    }

    @Test
    void shouldFailWhenPriceIsZero() {
        SampleDto dto = buildValidDto().toBuilder().price(BigDecimal.ZERO).build();

        Set<ConstraintViolation<SampleDto>> violations = validator.validate(dto);

        assertThat(violations).extracting(ConstraintViolation::getMessage).contains("Price must be greater than 0");
    }

    @Test
    void shouldFailWhenExternalIdIsNull() {
        SampleDto dto = buildValidDto().toBuilder().externalId(null).build();

        Set<ConstraintViolation<SampleDto>> violations = validator.validate(dto);

        assertThat(violations).extracting(ConstraintViolation::getMessage).contains("External ID is mandatory");
    }

    @Test
    void shouldFailWhenStatusIsNull() {
        SampleDto dto = buildValidDto().toBuilder().status(null).build();

        Set<ConstraintViolation<SampleDto>> violations = validator.validate(dto);

        assertThat(violations).extracting(ConstraintViolation::getMessage).contains("Status is mandatory");
    }

    @Test
    void shouldFailWhenDocumentIsNull() {
        SampleDto dto = buildValidDto().toBuilder().document(null).build();

        Set<ConstraintViolation<SampleDto>> violations = validator.validate(dto);

        assertThat(violations).extracting(ConstraintViolation::getMessage).contains("Document is mandatory");
    }

    @Test
    void shouldFailWhenManufacturedDateIsInFuture() {
        SampleDto dto = buildValidDto().toBuilder()
                .manufacturedDate(LocalDate.now().plusDays(1))
                .build();

        Set<ConstraintViolation<SampleDto>> violations = validator.validate(dto);

        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .contains("Manufactured date cannot be in the future");
    }

    private SampleDto buildValidDto() {
        return SampleDto.builder()
                .id(1L)
                .name("PRODUCT-001")
                .description("Sample product")
                .quantity(10)
                .stock(100L)
                .weight(5.5)
                .ratio(0.5f)
                .price(BigDecimal.valueOf(99.99))
                .active(true)
                .category('A')
                .manufacturedDate(LocalDate.now())
                .manufacturedTime(LocalTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(Instant.now())
                .document("SGVsbG8gV29ybGQ=".getBytes())
                .externalId(UUID.randomUUID())
                .comments("Comment")
                .status(Status.CREATED)
                .version(1L)
                .build();
    }
}
