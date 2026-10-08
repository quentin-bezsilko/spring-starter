package com.qbe.springstarter.testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.qbe.springstarter.dto.SampleDto;
import com.qbe.springstarter.entity.SampleEntity;
import com.qbe.springstarter.enums.Status;
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

        Page<SampleDto> result = sampleService.findAll(null, null, null, null, pageable);

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

        Page<SampleDto> result = sampleService.findAll(null, null, null, null, pageable);

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

        Page<SampleDto> firstPage = sampleService.findAll(null, null, null, null, firstPageable);

        assertThat(firstPage.getContent()).hasSize(1);
        assertThat(firstPage.getNumber()).isZero();

        Pageable secondPageable = PageRequest.of(1, 1, Sort.by("id").ascending());

        Page<SampleDto> secondPage = sampleService.findAll(null, null, null, null, secondPageable);

        assertThat(secondPage.getContent()).hasSize(1);
        assertThat(secondPage.getNumber()).isEqualTo(1);

        assertThat(secondPage.getContent().getFirst().id())
                .isNotEqualTo(firstPage.getContent().getFirst().id());
    }

    @Test
    void shouldSearchSampleByName() {
        String marker = "SEARCH-NAME-" + UUID.randomUUID();

        SampleDto expected = sampleService.create(buildSampleDto(marker));

        sampleService.create(buildSampleDto("OTHER-" + UUID.randomUUID()).toBuilder()
                .externalId(UUID.randomUUID())
                .build());

        Pageable pageable = PageRequest.of(0, 20, Sort.by("id").ascending());

        Page<SampleDto> result = sampleService.findAll(marker, null, null, null, pageable);

        assertThat(result.getContent()).extracting(SampleDto::id).contains(expected.id());

        assertThat(result.getContent()).extracting(SampleDto::name).contains(expected.name());
    }

    @Test
    void shouldSearchSampleCaseInsensitively() {
        String marker = "CaseSensitive-" + UUID.randomUUID();

        SampleDto expected = sampleService.create(buildSampleDto(marker));

        Pageable pageable = PageRequest.of(0, 20, Sort.by("id").ascending());

        Page<SampleDto> result = sampleService.findAll(marker.toLowerCase(), null, null, null, pageable);

        assertThat(result.getContent()).extracting(SampleDto::id).contains(expected.id());
    }

    @Test
    void shouldSearchSampleByDescription() {
        String marker = "UNIQUE-DESCRIPTION-" + UUID.randomUUID();

        SampleDto expected = sampleService.create(buildSampleDto("SERVICE-DESCRIPTION-" + UUID.randomUUID()).toBuilder()
                .description(marker)
                .externalId(UUID.randomUUID())
                .build());

        Pageable pageable = PageRequest.of(0, 20, Sort.by("id").ascending());

        Page<SampleDto> result = sampleService.findAll(marker, null, null, null, pageable);

        assertThat(result.getContent()).extracting(SampleDto::id).contains(expected.id());
    }

    @Test
    void shouldSearchSampleByComments() {
        String marker = "UNIQUE-COMMENT-" + UUID.randomUUID();

        SampleDto expected = sampleService.create(buildSampleDto("SERVICE-COMMENT-" + UUID.randomUUID()).toBuilder()
                .comments(marker)
                .externalId(UUID.randomUUID())
                .build());

        Pageable pageable = PageRequest.of(0, 20, Sort.by("id").ascending());

        Page<SampleDto> result = sampleService.findAll(marker, null, null, null, pageable);

        assertThat(result.getContent()).extracting(SampleDto::id).contains(expected.id());
    }

    @Test
    void shouldFilterSamplesByStatus() {
        String marker = UUID.randomUUID().toString();

        SampleDto activeSample = sampleService.create(buildSampleDto("ACTIVE-" + marker).toBuilder()
                .status(Status.ACTIVE)
                .externalId(UUID.randomUUID())
                .build());

        SampleDto archivedSample = sampleService.create(buildSampleDto("ARCHIVED-" + marker).toBuilder()
                .status(Status.ARCHIVED)
                .externalId(UUID.randomUUID())
                .build());

        Pageable pageable = PageRequest.of(0, 20, Sort.by("id").ascending());

        Page<SampleDto> result = sampleService.findAll(null, Status.ACTIVE, null, null, pageable);

        assertThat(result.getContent())
                .extracting(SampleDto::id)
                .contains(activeSample.id())
                .doesNotContain(archivedSample.id());

        assertThat(result.getContent()).allMatch(sample -> sample.status() == Status.ACTIVE);
    }

    @Test
    void shouldFilterSamplesByCategory() {
        String marker = UUID.randomUUID().toString();

        SampleDto categoryA = sampleService.create(buildSampleDto("CATEGORY-A-" + marker).toBuilder()
                .category('A')
                .externalId(UUID.randomUUID())
                .build());

        SampleDto categoryB = sampleService.create(buildSampleDto("CATEGORY-B-" + marker).toBuilder()
                .category('B')
                .externalId(UUID.randomUUID())
                .build());

        Pageable pageable = PageRequest.of(0, 20, Sort.by("id").ascending());

        Page<SampleDto> result = sampleService.findAll(null, null, 'A', null, pageable);

        assertThat(result.getContent())
                .extracting(SampleDto::id)
                .contains(categoryA.id())
                .doesNotContain(categoryB.id());

        assertThat(result.getContent())
                .allMatch(sample -> Character.valueOf('A').equals(sample.category()));
    }

    @Test
    void shouldFilterActiveSamples() {
        String marker = UUID.randomUUID().toString();

        SampleDto activeSample = sampleService.create(buildSampleDto("ACTIVE-TRUE-" + marker).toBuilder()
                .active(true)
                .externalId(UUID.randomUUID())
                .build());

        SampleDto inactiveSample = sampleService.create(buildSampleDto("ACTIVE-FALSE-" + marker).toBuilder()
                .active(false)
                .externalId(UUID.randomUUID())
                .build());

        Pageable pageable = PageRequest.of(0, 20, Sort.by("id").ascending());

        Page<SampleDto> result = sampleService.findAll(null, null, null, true, pageable);

        assertThat(result.getContent())
                .extracting(SampleDto::id)
                .contains(activeSample.id())
                .doesNotContain(inactiveSample.id());

        assertThat(result.getContent()).allMatch(SampleDto::active);
    }

    @Test
    void shouldFilterInactiveSamples() {
        String marker = UUID.randomUUID().toString();

        SampleDto activeSample = sampleService.create(buildSampleDto("INACTIVE-FILTER-TRUE-" + marker).toBuilder()
                .active(true)
                .externalId(UUID.randomUUID())
                .build());

        SampleDto inactiveSample = sampleService.create(buildSampleDto("INACTIVE-FILTER-FALSE-" + marker).toBuilder()
                .active(false)
                .externalId(UUID.randomUUID())
                .build());

        Pageable pageable = PageRequest.of(0, 20, Sort.by("id").ascending());

        Page<SampleDto> result = sampleService.findAll(null, null, null, false, pageable);

        assertThat(result.getContent())
                .extracting(SampleDto::id)
                .contains(inactiveSample.id())
                .doesNotContain(activeSample.id());

        assertThat(result.getContent()).allMatch(sample -> !sample.active());
    }

    @Test
    void shouldCombineSearchAndFilters() {
        String marker = "COMBINED-" + UUID.randomUUID();

        SampleDto expected = sampleService.create(buildSampleDto(marker + "-EXPECTED").toBuilder()
                .status(Status.ACTIVE)
                .category('A')
                .active(true)
                .externalId(UUID.randomUUID())
                .build());

        SampleDto wrongStatus = sampleService.create(buildSampleDto(marker + "-WRONG-STATUS").toBuilder()
                .status(Status.ARCHIVED)
                .category('A')
                .active(true)
                .externalId(UUID.randomUUID())
                .build());

        SampleDto wrongCategory = sampleService.create(buildSampleDto(marker + "-WRONG-CATEGORY").toBuilder()
                .status(Status.ACTIVE)
                .category('B')
                .active(true)
                .externalId(UUID.randomUUID())
                .build());

        SampleDto inactive = sampleService.create(buildSampleDto(marker + "-INACTIVE").toBuilder()
                .status(Status.ACTIVE)
                .category('A')
                .active(false)
                .externalId(UUID.randomUUID())
                .build());

        Pageable pageable = PageRequest.of(0, 20, Sort.by("id").ascending());

        Page<SampleDto> result = sampleService.findAll(marker, Status.ACTIVE, 'A', true, pageable);

        assertThat(result.getContent())
                .extracting(SampleDto::id)
                .contains(expected.id())
                .doesNotContain(wrongStatus.id(), wrongCategory.id(), inactive.id());
    }

    @Test
    void shouldSortSamplesByNameAscending() {
        String marker = "SORT-NAME-" + UUID.randomUUID();

        SampleDto sampleC = sampleService.create(buildSampleDto(marker + "-C").toBuilder()
                .externalId(UUID.randomUUID())
                .build());

        SampleDto sampleA = sampleService.create(buildSampleDto(marker + "-A").toBuilder()
                .externalId(UUID.randomUUID())
                .build());

        SampleDto sampleB = sampleService.create(buildSampleDto(marker + "-B").toBuilder()
                .externalId(UUID.randomUUID())
                .build());

        Pageable pageable = PageRequest.of(0, 20, Sort.by("name").ascending());

        Page<SampleDto> result = sampleService.findAll(marker, null, null, null, pageable);

        assertThat(result.getContent())
                .extracting(SampleDto::id)
                .containsExactly(sampleA.id(), sampleB.id(), sampleC.id());
    }

    @Test
    void shouldSortSamplesByNameDescending() {
        String marker = "SORT-DESC-" + UUID.randomUUID();

        SampleDto sampleA = sampleService.create(buildSampleDto(marker + "-A").toBuilder()
                .externalId(UUID.randomUUID())
                .build());

        SampleDto sampleB = sampleService.create(buildSampleDto(marker + "-B").toBuilder()
                .externalId(UUID.randomUUID())
                .build());

        SampleDto sampleC = sampleService.create(buildSampleDto(marker + "-C").toBuilder()
                .externalId(UUID.randomUUID())
                .build());

        Pageable pageable = PageRequest.of(0, 20, Sort.by("name").descending());

        Page<SampleDto> result = sampleService.findAll(marker, null, null, null, pageable);

        assertThat(result.getContent())
                .extracting(SampleDto::id)
                .containsExactly(sampleC.id(), sampleB.id(), sampleA.id());
    }

    @Test
    void shouldReturnEmptyPageWhenSearchDoesNotMatch() {
        Pageable pageable = PageRequest.of(0, 20, Sort.by("id").ascending());

        Page<SampleDto> result =
                sampleService.findAll("DOES-NOT-EXIST-" + UUID.randomUUID(), null, null, null, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    void shouldIgnoreBlankSearch() {
        SampleDto created = sampleService.create(buildSampleDto("BLANK-SEARCH-" + UUID.randomUUID()));

        Pageable pageable = PageRequest.of(0, 100, Sort.by("id").ascending());

        Page<SampleDto> result = sampleService.findAll("   ", null, null, null, pageable);

        assertThat(result.getContent()).extracting(SampleDto::id).contains(created.id());
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
