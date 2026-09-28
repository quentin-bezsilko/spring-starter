package com.qbe.springstarter.testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.qbe.springstarter.dto.SampleDto;
import com.qbe.springstarter.error.NotFoundException;
import com.qbe.springstarter.repository.SampleRepository;
import com.qbe.springstarter.service.SampleService;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class TestSampleServiceIT extends TestAbstractIntegration {

    @Autowired
    private SampleService sampleService;

    @Autowired
    private SampleRepository sampleRepository;

    @Test
    void shouldCreateSample() {
        SampleDto dto = buildSampleDto("SERVICE-PRODUCT-001-" + UUID.randomUUID());
        SampleDto created = sampleService.create(dto);

        assertThat(created).isNotNull();
        assertThat(created.id()).isNotNull();
        assertThat(created.name()).isEqualTo(dto.name());
        assertThat(created.description()).isEqualTo(dto.description());
        assertThat(created.status()).isEqualTo(dto.status());
    }

    @Test
    void shouldFindSampleById() {
        SampleDto created = sampleService.create(buildSampleDto("SERVICE-PRODUCT-002-" + UUID.randomUUID()));
        SampleDto result = sampleService.findById(created.id());

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(created.id());
        assertThat(result.name()).isEqualTo(created.name());
    }

    @Test
    void shouldFindAllSamples() {
        sampleService.create(buildSampleDto("SERVICE-PRODUCT-003-" + UUID.randomUUID()));

        SampleDto secondSample = buildSampleDto("SECOND_SERVICE_PRODUCT-" + UUID.randomUUID()).toBuilder()
                .externalId(UUID.randomUUID())
                .build();

        sampleService.create(secondSample);
        assertThat(sampleService.findAll()).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void shouldUpdateSample() {
        SampleDto created = sampleService.create(buildSampleDto("SERVICE-PRODUCT-004-" + UUID.randomUUID()));

        UUID uuidUpdate = UUID.randomUUID();
        SampleDto updatedDto = buildSampleDto("UPDATED_SERVICE_PRODUCT-" + uuidUpdate).toBuilder()
                .description("Updated description")
                .quantity(200)
                .stock(500L)
                .weight(20.0)
                .ratio(0.90f)
                .price(BigDecimal.valueOf(149.99))
                .category('B')
                .comments("Updated comment")
                .externalId(UUID.randomUUID())
                .build();

        SampleDto updated = sampleService.update(created.id(), updatedDto);

        assertThat(updated.name()).isEqualTo("UPDATED_SERVICE_PRODUCT-" + uuidUpdate);
        assertThat(updated.description()).isEqualTo("Updated description");
        assertThat(updated.quantity()).isEqualTo(200);
    }

    @Test
    void shouldDeleteSample() {
        SampleDto created = sampleService.create(buildSampleDto("SERVICE-PRODUCT-005" + UUID.randomUUID()));
        sampleService.delete(created.id());

        assertThat(sampleRepository.existsById(created.id())).isFalse();
    }

    @Test
    void shouldThrowNotFoundWhenIdDoesNotExist() {
        assertThatThrownBy(() -> sampleService.findById(999999L)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldThrowNotFoundWhenUpdatingUnknownSample() {
        assertThatThrownBy(() -> sampleService.update(999999L, buildSampleDto("SERVICE-PRODUCT-006")))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldThrowNotFoundWhenDeletingUnknownSample() {
        assertThatThrownBy(() -> sampleService.delete(999999L)).isInstanceOf(NotFoundException.class);
    }
}
