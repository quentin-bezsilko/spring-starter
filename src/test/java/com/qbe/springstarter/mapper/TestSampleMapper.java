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
        assertThat(dto.comments()).isEqualTo(entity.getComments());
        assertThat(dto.status()).isEqualTo(entity.getStatus());
        assertThat(dto.version()).isEqualTo(entity.getVersion());
    }

    @Test
    void shouldMapDtoToEntity() {
        SampleDto dto = buildDto();

        SampleEntity entity = sampleMapper.toEntity(dto);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(dto.id());
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
        assertThat(entity.getCreatedAt()).isEqualTo(dto.createdAt());
        assertThat(entity.getUpdatedAt()).isEqualTo(dto.updatedAt());
        assertThat(entity.getExternalId()).isEqualTo(dto.externalId());
        assertThat(entity.getComments()).isEqualTo(dto.comments());
        assertThat(entity.getStatus()).isEqualTo(dto.status());
        assertThat(entity.getVersion()).isEqualTo(dto.version());
    }

    @Test
    void shouldUpdateEntityFromDto() {
        SampleEntity entity = buildEntity();

        UUID uuid = UUID.randomUUID();
        Instant now = Instant.now();

        SampleDto dto = SampleDto.builder()
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
                .updatedAt(now)
                .externalId(uuid)
                .comments("Updated comment")
                .status(Status.CREATED)
                .version(2L)
                .build();

        sampleMapper.updateEntityFromDto(dto, entity);

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
        assertThat(entity.getCreatedAt()).isEqualTo(LocalDateTime.of(2025, 1, 1, 12, 0));
        assertThat(entity.getUpdatedAt()).isEqualTo(now);
        assertThat(entity.getExternalId()).isEqualTo(uuid);
        assertThat(entity.getComments()).isEqualTo("Updated comment");
        assertThat(entity.getStatus()).isEqualTo(Status.CREATED);
        assertThat(entity.getVersion()).isEqualTo(2L);
    }

    @Test
    void shouldIgnoreNullValuesDuringUpdate() {
        SampleEntity entity = buildEntity();

        SampleDto dto = SampleDto.builder()
                .name("UPDATED_NAME")
                .description(null)
                .quantity(null)
                .build();

        sampleMapper.updateEntityFromDto(dto, entity);

        assertThat(entity.getName()).isEqualTo("UPDATED_NAME");

        assertThat(entity.getDescription()).isEqualTo("Sample description");

        assertThat(entity.getQuantity()).isEqualTo(100);
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
                .manufacturedDate(LocalDate.now())
                .manufacturedTime(LocalTime.of(10, 30))
                .createdAt(LocalDateTime.now())
                .updatedAt(Instant.now())
                .externalId(UUID.randomUUID())
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
        entity.setManufacturedDate(LocalDate.now());
        entity.setManufacturedTime(LocalTime.of(10, 30));
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(Instant.now());
        entity.setExternalId(UUID.randomUUID());
        entity.setComments("Comment");
        entity.setStatus(Status.CREATED);
        entity.setVersion(1L);

        return entity;
    }
}
