package com.qbe.springstarter.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.qbe.springstarter.dto.SampleDto;
import com.qbe.springstarter.entity.SampleEntity;
import com.qbe.springstarter.enums.Status;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class TestSampleMapper {

    private final SampleMapper sampleMapper = Mappers.getMapper(SampleMapper.class);

    @Test
    void shouldMapEntityToDto() {
        SampleEntity entity = buildEntity();

        SampleDto dto = sampleMapper.toDto(entity);

        assertThat(dto).isNotNull();
        assertThat(dto.id()).isEqualTo(entity.getId());
        assertThat(dto.name()).isEqualTo(entity.getName());
        assertThat(dto.description()).isEqualTo(entity.getDescription());
        assertThat(dto.quantity()).isEqualTo(entity.getQuantity());
        assertThat(dto.stock()).isEqualTo(entity.getStock());
        assertThat(dto.weight()).isEqualTo(entity.getWeight());
        assertThat(dto.ratio()).isEqualTo(entity.getRatio());
        assertThat(dto.price()).isEqualTo(entity.getPrice());
        assertThat(dto.active()).isEqualTo(entity.getActive());
        assertThat(dto.category()).isEqualTo(entity.getCategory());
        assertThat(dto.manufacturedDate()).isEqualTo(entity.getManufacturedDate());
        assertThat(dto.manufacturedTime()).isEqualTo(entity.getManufacturedTime());
        assertThat(dto.createdAt()).isEqualTo(entity.getCreatedAt());
        assertThat(dto.updatedAt()).isEqualTo(entity.getUpdatedAt());
        assertThat(dto.externalId()).isEqualTo(entity.getExternalId());
        assertThat(dto.document()).isEqualTo(entity.getDocument());
        assertThat(dto.comments()).isEqualTo(entity.getComments());
        assertThat(dto.status()).isEqualTo(entity.getStatus());
        assertThat(dto.version()).isEqualTo(entity.getVersion());
    }

    @Test
    void shouldMapDtoToEntityAndIgnoreTechnicalFields() {
        SampleDto dto = buildDto();

        SampleEntity entity = sampleMapper.toEntity(dto);

        assertThat(entity).isNotNull();
        assertThat(entity.getName()).isEqualTo(dto.name());
        assertThat(entity.getDescription()).isEqualTo(dto.description());
        assertThat(entity.getQuantity()).isEqualTo(dto.quantity());
        assertThat(entity.getStock()).isEqualTo(dto.stock());
        assertThat(entity.getWeight()).isEqualTo(dto.weight());
        assertThat(entity.getRatio()).isEqualTo(dto.ratio());
        assertThat(entity.getPrice()).isEqualTo(dto.price());
        assertThat(entity.getActive()).isEqualTo(dto.active());
        assertThat(entity.getCategory()).isEqualTo(dto.category());
        assertThat(entity.getManufacturedDate()).isEqualTo(dto.manufacturedDate());
        assertThat(entity.getManufacturedTime()).isEqualTo(dto.manufacturedTime());
        assertThat(entity.getExternalId()).isEqualTo(dto.externalId());
        assertThat(entity.getDocument()).isEqualTo(dto.document());
        assertThat(entity.getComments()).isEqualTo(dto.comments());
        assertThat(entity.getStatus()).isEqualTo(dto.status());

        // Champs techniques explicitement ignorés par MapStruct.
        assertThat(entity.getId()).isNull();
        assertThat(entity.getCreatedAt()).isNull();
        assertThat(entity.getUpdatedAt()).isNull();
        assertThat(entity.getVersion()).isNull();
    }

    @Test
    void shouldUpdateEntityFromDtoAndPreserveTechnicalFields() {
        SampleEntity entity = buildEntity();

        Long originalId = entity.getId();
        LocalDateTime originalCreatedAt = entity.getCreatedAt();
        Instant originalUpdatedAt = entity.getUpdatedAt();
        UUID originalExternalId = entity.getExternalId();
        Long originalVersion = entity.getVersion();

        UUID dtoExternalId = UUID.randomUUID();
        Instant dtoUpdatedAt = Instant.now().plusSeconds(60);

        SampleDto dto = SampleDto.builder()
                .id(999L)
                .name("UPDATED_NAME")
                .description("Updated description")
                .quantity(999)
                .stock(9999L)
                .weight(99.99)
                .ratio(0.99f)
                .price(BigDecimal.valueOf(199.99))
                .active(false)
                .category('B')
                .manufacturedDate(LocalDate.of(2025, 1, 1))
                .manufacturedTime(LocalTime.of(12, 0))
                .createdAt(LocalDateTime.of(2025, 1, 1, 12, 0))
                .updatedAt(dtoUpdatedAt)
                .externalId(dtoExternalId)
                .document(new byte[] {9, 8, 7})
                .comments("Updated comment")
                .status(Status.CREATED)
                .version(999L)
                .build();

        sampleMapper.updateEntityFromDto(dto, entity);

        // Champs métier modifiables.
        assertThat(entity.getName()).isEqualTo("UPDATED_NAME");
        assertThat(entity.getDescription()).isEqualTo("Updated description");
        assertThat(entity.getQuantity()).isEqualTo(999);
        assertThat(entity.getStock()).isEqualTo(9999L);
        assertThat(entity.getWeight()).isEqualTo(99.99);
        assertThat(entity.getRatio()).isEqualTo(0.99f);
        assertThat(entity.getPrice()).isEqualByComparingTo("199.99");
        assertThat(entity.getActive()).isFalse();
        assertThat(entity.getCategory()).isEqualTo('B');
        assertThat(entity.getManufacturedDate()).isEqualTo(LocalDate.of(2025, 1, 1));
        assertThat(entity.getManufacturedTime()).isEqualTo(LocalTime.of(12, 0));
        assertThat(entity.getDocument()).containsExactly(9, 8, 7);
        assertThat(entity.getComments()).isEqualTo("Updated comment");
        assertThat(entity.getStatus()).isEqualTo(Status.CREATED);
        // Champs techniques protégés.
        assertThat(entity.getId()).isEqualTo(originalId);
        assertThat(entity.getCreatedAt()).isEqualTo(originalCreatedAt);
        assertThat(entity.getUpdatedAt()).isEqualTo(originalUpdatedAt);
        assertThat(entity.getExternalId()).isEqualTo(originalExternalId);
        assertThat(entity.getVersion()).isEqualTo(originalVersion);
    }

    @Test
    void shouldIgnoreNullValuesDuringUpdate() {
        SampleEntity entity = buildEntity();

        SampleDto dto = SampleDto.builder()
                .name("UPDATED_NAME")
                .description(null)
                .quantity(null)
                .stock(null)
                .weight(null)
                .ratio(null)
                .price(null)
                .comments(null)
                .build();

        sampleMapper.updateEntityFromDto(dto, entity);

        assertThat(entity.getName()).isEqualTo("UPDATED_NAME");
        assertThat(entity.getDescription()).isEqualTo("Sample description");
        assertThat(entity.getQuantity()).isEqualTo(100);
        assertThat(entity.getStock()).isEqualTo(1000L);
        assertThat(entity.getWeight()).isEqualTo(10.5);
        assertThat(entity.getRatio()).isEqualTo(0.75f);
        assertThat(entity.getPrice()).isEqualByComparingTo("99.99");
        assertThat(entity.getComments()).isEqualTo("Comment");
    }

    @Test
    void shouldPreserveTechnicalFieldsWhenDtoContainsDifferentValues() {
        SampleEntity entity = buildEntity();

        Long id = entity.getId();
        UUID externalId = entity.getExternalId();
        LocalDateTime createdAt = entity.getCreatedAt();
        Instant updatedAt = entity.getUpdatedAt();
        Long version = entity.getVersion();

        SampleDto dto = SampleDto.builder()
                .id(999L)
                .createdAt(LocalDateTime.of(2000, 1, 1, 0, 0))
                .updatedAt(Instant.parse("2000-01-01T00:00:00Z"))
                .externalId(UUID.randomUUID())
                .version(999L)
                .build();

        sampleMapper.updateEntityFromDto(dto, entity);

        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getCreatedAt()).isEqualTo(createdAt);
        assertThat(entity.getUpdatedAt()).isEqualTo(updatedAt);
        assertThat(entity.getExternalId()).isEqualTo(externalId);
        assertThat(entity.getVersion()).isEqualTo(version);
    }

    private SampleDto buildDto() {
        return SampleDto.builder()
                .id(1L)
                .name("PRODUCT-001")
                .description("Sample description")
                .quantity(100)
                .stock(1000L)
                .weight(10.5)
                .ratio(0.75f)
                .price(BigDecimal.valueOf(99.99))
                .active(true)
                .category('A')
                .manufacturedDate(LocalDate.of(2025, 1, 1))
                .manufacturedTime(LocalTime.of(10, 30))
                .createdAt(LocalDateTime.of(2025, 1, 1, 10, 30))
                .updatedAt(Instant.parse("2025-01-01T10:30:00Z"))
                .externalId(UUID.randomUUID())
                .document(new byte[] {1, 2, 3})
                .comments("Comment")
                .status(Status.CREATED)
                .version(1L)
                .build();
    }

    private SampleEntity buildEntity() {
        SampleEntity entity = new SampleEntity();
        entity.setId(1L);
        entity.setName("PRODUCT-001");
        entity.setDescription("Sample description");
        entity.setQuantity(100);
        entity.setStock(1000L);
        entity.setWeight(10.5);
        entity.setRatio(0.75f);
        entity.setPrice(BigDecimal.valueOf(99.99));
        entity.setActive(true);
        entity.setCategory('A');
        entity.setManufacturedDate(LocalDate.of(2025, 1, 1));
        entity.setManufacturedTime(LocalTime.of(10, 30));
        entity.setCreatedAt(LocalDateTime.of(2025, 1, 1, 10, 30));
        entity.setUpdatedAt(Instant.parse("2025-01-01T10:30:00Z"));
        entity.setExternalId(UUID.randomUUID());
        entity.setDocument(new byte[] {1, 2, 3});
        entity.setComments("Comment");
        entity.setStatus(Status.CREATED);
        entity.setVersion(1L);

        return entity;
    }
}
