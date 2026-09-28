package com.qbe.springstarter.dto;

import com.qbe.springstarter.enums.Status;
import com.qbe.springstarter.validator.ValidSample;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;
import lombok.Builder;

@Builder(toBuilder = true)
@ValidSample
public record SampleDto(
        Long id,
        @NotBlank(message = "Name is mandatory")
                @Size(min = 3, max = 100, message = "Name must contain between 3 and 100 characters")
                @Pattern(
                        regexp = "^[A-Za-z0-9_-]+$",
                        message = "Name can only contain letters, numbers, underscores and hyphens")
                String name,
        @Size(max = 500, message = "Description cannot exceed 500 characters") String description,
        @NotNull(message = "Quantity is mandatory")
                @Positive(message = "Quantity must be greater than 0")
                @Max(value = 100000, message = "Quantity cannot exceed 100000")
                Integer quantity,
        @PositiveOrZero(message = "Stock must be positive or zero") Long stock,
        @DecimalMin(value = "0.0", inclusive = false) @DecimalMax(value = "1000.0") Double weight,
        @DecimalMin(value = "0.0") @DecimalMax(value = "1.0") Float ratio,
        @NotNull(message = "Price is mandatory")
                @DecimalMin(value = "0.01", message = "Price must be greater than 0")
                @Digits(integer = 13, fraction = 2)
                BigDecimal price,
        @NotNull(message = "Active flag is mandatory") Boolean active,
        @NotNull(message = "Category is mandatory") Character category,
        @PastOrPresent(message = "Manufactured date cannot be in the future") LocalDate manufacturedDate,
        LocalTime manufacturedTime,
        @PastOrPresent(message = "Created date cannot be in the future") LocalDateTime createdAt,
        @PastOrPresent(message = "Updated date cannot be in the future") Instant updatedAt,
        @NotNull(message = "External ID is mandatory") UUID externalId,
        @NotNull(message = "Document is mandatory") byte[] document,
        @Size(max = 5000, message = "Comments cannot exceed 5000 characters") String comments,
        @NotNull(message = "Status is mandatory") Status status,
        Long version)
        implements Serializable {}
