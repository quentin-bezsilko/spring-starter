package com.qbe.springstarter.testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.qbe.springstarter.dto.SampleDto;
import com.qbe.springstarter.entity.SampleEntity;
import com.qbe.springstarter.error.NotFoundException;
import com.qbe.springstarter.error.VersionConflictException;
import com.qbe.springstarter.repository.SampleRepository;
import com.qbe.springstarter.service.SampleService;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

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
        assertThat(created.version()).isZero();
    }

    @Test
    void shouldFindSampleById() {
        SampleDto created = sampleService.create(buildSampleDto("SERVICE-PRODUCT-002-" + UUID.randomUUID()));

        SampleDto result = sampleService.findById(created.id());

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(created.id());
        assertThat(result.name()).isEqualTo(created.name());
        assertThat(result.version()).isEqualTo(created.version());
    }

    @Test
    void shouldFindAllSamplesWithPagination() {
        SampleDto first = sampleService.create(buildSampleDto("SERVICE-PRODUCT-003-" + UUID.randomUUID()));

        SampleDto secondDto = buildSampleDto("SECOND_SERVICE_PRODUCT-" + UUID.randomUUID()).toBuilder()
                .externalId(UUID.randomUUID())
                .build();

        SampleDto second = sampleService.create(secondDto);

        Pageable pageable = PageRequest.of(0, 20, Sort.by("id").ascending());

        Page<SampleDto> result = sampleService.findAll(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).isNotEmpty();
        assertThat(result.getContent()).extracting(SampleDto::id).contains(first.id(), second.id());
        assertThat(result.getNumber()).isZero();
        assertThat(result.getSize()).isEqualTo(20);
        assertThat(result.getTotalElements()).isGreaterThanOrEqualTo(2);
        assertThat(result.getTotalPages()).isGreaterThanOrEqualTo(1);
        assertThat(result.isFirst()).isTrue();
    }

    @Test
    void shouldRespectRequestedPageSize() {
        sampleService.create(buildSampleDto("SERVICE-PAGE-001-" + UUID.randomUUID()));

        sampleService.create(buildSampleDto("SERVICE-PAGE-002-" + UUID.randomUUID()).toBuilder()
                .externalId(UUID.randomUUID())
                .build());

        sampleService.create(buildSampleDto("SERVICE-PAGE-003-" + UUID.randomUUID()).toBuilder()
                .externalId(UUID.randomUUID())
                .build());

        Pageable pageable = PageRequest.of(0, 2, Sort.by("id").ascending());

        Page<SampleDto> result = sampleService.findAll(pageable);
        assertThat(result.getNumber()).isZero();
        assertThat(result.getSize()).isEqualTo(2);
        assertThat(result.getNumberOfElements()).isLessThanOrEqualTo(2);
        assertThat(result.getContent()).hasSizeLessThanOrEqualTo(2);
    }

    @Test
    void shouldReturnNextPage() {
        sampleService.create(buildSampleDto("SERVICE-NEXT-PAGE-001-" + UUID.randomUUID()));

        sampleService.create(buildSampleDto("SERVICE-NEXT-PAGE-002-" + UUID.randomUUID()).toBuilder()
                .externalId(UUID.randomUUID())
                .build());

        Pageable firstPageable = PageRequest.of(0, 1, Sort.by("id").ascending());

        Page<SampleDto> firstPage = sampleService.findAll(firstPageable);

        assertThat(firstPage.getContent()).hasSize(1);
        assertThat(firstPage.getNumber()).isZero();

        Pageable secondPageable = PageRequest.of(1, 1, Sort.by("id").ascending());
        Page<SampleDto> secondPage = sampleService.findAll(secondPageable);

        assertThat(secondPage.getContent()).hasSize(1);
        assertThat(secondPage.getNumber()).isEqualTo(1);
        assertThat(secondPage.getContent().getFirst().id())
                .isNotEqualTo(firstPage.getContent().getFirst().id());
    }

    @Test
    void shouldUpdateSample() {
        SampleDto created = sampleService.create(buildSampleDto("SERVICE-PRODUCT-004-" + UUID.randomUUID()));

        assertThat(created.version()).isZero();

        UUID uuidUpdate = UUID.randomUUID();

        SampleDto updatedDto = created.toBuilder()
                .name("UPDATED_SERVICE_PRODUCT-" + uuidUpdate)
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
        SampleEntity persisted = sampleRepository.findById(created.id()).orElseThrow();

        assertThat(updated.name()).isEqualTo("UPDATED_SERVICE_PRODUCT-" + uuidUpdate);
        assertThat(updated.description()).isEqualTo("Updated description");
        assertThat(updated.quantity()).isEqualTo(200);
        assertThat(persisted.getVersion()).isEqualTo(1L);
        assertThat(updated.version()).isEqualTo(1L);
    }

    @Test
    void shouldThrowVersionConflictWhenUpdatingWithOutdatedVersion() {
        SampleDto created = sampleService.create(buildSampleDto("SERVICE-VERSION-CONFLICT-" + UUID.randomUUID()));

        SampleDto firstUpdate = created.toBuilder().name("SERVICE-FIRST-UPDATE").build();
        SampleDto updated = sampleService.update(created.id(), firstUpdate);

        assertThat(updated.version()).isGreaterThan(created.version());

        SampleDto outdatedUpdate =
                created.toBuilder().name("SERVICE-OUTDATED-UPDATE").build();

        assertThatThrownBy(() -> sampleService.update(created.id(), outdatedUpdate))
                .isInstanceOf(VersionConflictException.class);
    }

    @Test
    void shouldDeleteSample() {
        SampleDto created = sampleService.create(buildSampleDto("SERVICE-PRODUCT-005-" + UUID.randomUUID()));
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
