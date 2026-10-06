package com.qbe.springstarter.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.qbe.springstarter.dto.SampleDto;
import com.qbe.springstarter.entity.SampleEntity;
import com.qbe.springstarter.error.NotFoundException;
import com.qbe.springstarter.error.TechnicalException;
import com.qbe.springstarter.error.VersionConflictException;
import com.qbe.springstarter.mapper.SampleMapper;
import com.qbe.springstarter.repository.SampleRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class TestSampleService {

    @Mock
    private SampleRepository sampleRepository;

    @Mock
    private SampleMapper sampleMapper;

    private SampleService sampleService;

    private SampleDto sampleDto;
    private SampleEntity sampleEntity;

    @BeforeEach
    void setUp() {
        sampleDto = SampleDto.builder().id(1L).name("Sample").version(0L).build();

        sampleEntity = new SampleEntity();
        sampleEntity.setId(1L);
        sampleEntity.setName("Sample");
        sampleEntity.setVersion(0L);

        sampleService = new SampleService(sampleRepository, sampleMapper);
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("Doit créer une entité")
        void shouldCreateEntity() {
            when(sampleMapper.toEntity(sampleDto)).thenReturn(sampleEntity);
            when(sampleRepository.save(any(SampleEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(sampleMapper.toDto(any(SampleEntity.class))).thenReturn(sampleDto);

            SampleDto result = sampleService.create(sampleDto);

            assertEquals(sampleDto, result);

            verify(sampleMapper).toEntity(sampleDto);
            verify(sampleRepository).save(sampleEntity);
            verify(sampleMapper).toDto(sampleEntity);
        }

        @Test
        @DisplayName("Doit lever une TechnicalException lorsqu'une erreur survient")
        void shouldThrowTechnicalException() {
            when(sampleMapper.toEntity(sampleDto)).thenThrow(new RuntimeException("boom"));
            assertThrows(TechnicalException.class, () -> sampleService.create(sampleDto));
            verify(sampleRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("Doit retourner le DTO lorsque l'entité existe")
        void shouldReturnDtoWhenEntityExists() {
            when(sampleRepository.findById(1L)).thenReturn(Optional.of(sampleEntity));
            when(sampleMapper.toDto(sampleEntity)).thenReturn(sampleDto);

            SampleDto result = sampleService.findById(1L);

            assertEquals(sampleDto, result);

            verify(sampleRepository).findById(1L);
            verify(sampleMapper).toDto(sampleEntity);
        }

        @Test
        @DisplayName("Doit lever une NotFoundException lorsque l'entité n'existe pas")
        void shouldThrowNotFoundExceptionWhenEntityDoesNotExist() {
            when(sampleRepository.findById(1L)).thenReturn(Optional.empty());
            assertThrows(NotFoundException.class, () -> sampleService.findById(1L));
            verify(sampleMapper, never()).toDto(any());
        }
    }

    @Nested
    @DisplayName("findAll")
    class FindAll {

        @Test
        @DisplayName("Doit retourner une page de DTO")
        void shouldReturnPageOfDtos() {
            SampleEntity entity2 = new SampleEntity();
            entity2.setId(2L);
            entity2.setName("Sample2");

            SampleDto dto2 =
                    SampleDto.builder().id(2L).name("Sample2").version(0L).build();

            Pageable pageable = PageRequest.of(0, 20, Sort.by("id").ascending());

            Page<SampleEntity> entityPage = new PageImpl<>(List.of(sampleEntity, entity2), pageable, 2);

            when(sampleRepository.findAll(pageable)).thenReturn(entityPage);
            when(sampleMapper.toDto(sampleEntity)).thenReturn(sampleDto);
            when(sampleMapper.toDto(entity2)).thenReturn(dto2);

            Page<SampleDto> result = sampleService.findAll(pageable);

            assertEquals(2, result.getContent().size());
            assertEquals(sampleDto, result.getContent().get(0));
            assertEquals(dto2, result.getContent().get(1));
            assertEquals(2, result.getTotalElements());
            assertEquals(1, result.getTotalPages());
            assertEquals(0, result.getNumber());
            assertEquals(20, result.getSize());
            assertTrue(result.isFirst());
            assertTrue(result.isLast());

            verify(sampleRepository).findAll(pageable);
            verify(sampleMapper).toDto(sampleEntity);
            verify(sampleMapper).toDto(entity2);
        }

        @Test
        @DisplayName("Doit transmettre la pagination au repository")
        void shouldUseRequestedPagination() {
            Pageable pageable = PageRequest.of(2, 5, Sort.by("name").descending());

            Page<SampleEntity> entityPage = new PageImpl<>(List.of(), pageable, 12);

            when(sampleRepository.findAll(pageable)).thenReturn(entityPage);

            Page<SampleDto> result = sampleService.findAll(pageable);

            assertEquals(2, result.getNumber());
            assertEquals(5, result.getSize());
            assertEquals(12, result.getTotalElements());
            assertEquals(3, result.getTotalPages());
            assertFalse(result.isFirst());
            assertTrue(result.isLast());

            verify(sampleRepository).findAll(pageable);
        }

        @Test
        @DisplayName("Doit retourner une page vide lorsqu'aucune entité n'existe")
        void shouldReturnEmptyPage() {
            Pageable pageable = PageRequest.of(0, 20, Sort.by("id").ascending());
            Page<SampleEntity> entityPage = new PageImpl<>(List.of(), pageable, 0);

            when(sampleRepository.findAll(pageable)).thenReturn(entityPage);

            Page<SampleDto> result = sampleService.findAll(pageable);

            assertTrue(result.isEmpty());
            assertEquals(0, result.getTotalElements());
            assertEquals(0, result.getTotalPages());
            assertEquals(0, result.getNumber());
            assertEquals(20, result.getSize());

            verify(sampleRepository).findAll(pageable);
            verify(sampleMapper, never()).toDto(any());
        }

        @Test
        @DisplayName("Doit propager l'erreur lorsqu'une erreur survient")
        void shouldHandleFindAllError() {
            Pageable pageable = PageRequest.of(0, 20);
            RuntimeException exception = new RuntimeException("Database error");

            when(sampleRepository.findAll(pageable)).thenThrow(exception);

            RuntimeException thrown = assertThrows(RuntimeException.class, () -> sampleService.findAll(pageable));

            assertEquals(exception, thrown);
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("Doit mettre à jour une entité existante")
        void shouldUpdateEntity() {
            when(sampleRepository.findById(1L)).thenReturn(Optional.of(sampleEntity));
            when(sampleRepository.saveAndFlush(sampleEntity)).thenReturn(sampleEntity);
            when(sampleMapper.toDto(sampleEntity)).thenReturn(sampleDto);

            SampleDto result = sampleService.update(1L, sampleDto);

            assertEquals(sampleDto, result);

            verify(sampleMapper).updateEntityFromDto(sampleDto, sampleEntity);
            verify(sampleRepository).saveAndFlush(sampleEntity);
        }

        @Test
        @DisplayName("Doit lever une VersionConflictException lorsque la version est obsolète")
        void shouldThrowVersionConflictException() {
            sampleEntity.setVersion(1L);

            SampleDto outdatedDto = sampleDto.toBuilder().version(0L).build();

            when(sampleRepository.findById(1L)).thenReturn(Optional.of(sampleEntity));

            assertThrows(VersionConflictException.class, () -> sampleService.update(1L, outdatedDto));
            verify(sampleMapper, never()).updateEntityFromDto(any(), any());
            verify(sampleRepository, never()).saveAndFlush(any());
        }

        @Test
        @DisplayName("Doit lever une NotFoundException lorsque l'entité n'existe pas")
        void shouldThrowNotFoundExceptionWhenUpdatingMissingEntity() {
            when(sampleRepository.findById(1L)).thenReturn(Optional.empty());
            assertThrows(NotFoundException.class, () -> sampleService.update(1L, sampleDto));
            verify(sampleRepository, never()).saveAndFlush(any());
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("Doit supprimer une entité existante")
        void shouldDeleteEntity() {
            when(sampleRepository.existsById(1L)).thenReturn(true);
            sampleService.delete(1L);
            verify(sampleRepository).deleteById(1L);
        }

        @Test
        @DisplayName("Doit lever une NotFoundException lorsque l'entité n'existe pas")
        void shouldThrowNotFoundExceptionWhenDeletingMissingEntity() {
            when(sampleRepository.existsById(1L)).thenReturn(false);
            assertThrows(NotFoundException.class, () -> sampleService.delete(1L));
            verify(sampleRepository, never()).deleteById(anyLong());
        }
    }
}
