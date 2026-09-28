package com.qbe.springstarter.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.qbe.springstarter.config.SampleMetricsConfig;
import com.qbe.springstarter.dto.SampleDto;
import com.qbe.springstarter.entity.SampleEntity;
import com.qbe.springstarter.error.NotFoundException;
import com.qbe.springstarter.error.TechnicalException;
import com.qbe.springstarter.mapper.SampleMapper;
import com.qbe.springstarter.repository.SampleRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TestSampleService {

    @Mock
    private Scope scope;

    @Mock
    private SpanBuilder spanBuilder;

    @Mock
    private Span span;

    @Mock
    private Tracer tracer;

    @Mock
    private SampleRepository sampleRepository;

    @Mock
    private SampleMapper sampleMapper;

    @Mock
    private SampleMetricsConfig metrics;

    @Mock
    private Timer createTimer;

    @Mock
    private Timer findByIdTimer;

    @Mock
    private Timer findAllTimer;

    @Mock
    private Timer updateTimer;

    @Mock
    private Timer deleteTimer;

    @Mock
    private Counter createSuccess;

    @Mock
    private Counter createError;

    @Mock
    private Counter findSuccess;

    @Mock
    private Counter findError;

    @Mock
    private Counter findNotFound;

    @Mock
    private Counter findAllSuccess;

    @Mock
    private Counter findAllError;

    @Mock
    private Counter updateSuccess;

    @Mock
    private Counter updateError;

    @Mock
    private Counter updateNotFound;

    @Mock
    private Counter deleteSuccess;

    @Mock
    private Counter deleteError;

    @Mock
    private Counter deleteNotFound;

    @Mock
    private Counter technicalErrors;

    private SampleService sampleService;

    private SampleDto sampleDto;
    private SampleEntity sampleEntity;

    @BeforeEach
    void setUp() {
        sampleService = new SampleService(sampleRepository, sampleMapper, tracer, metrics);
        sampleDto = SampleDto.builder().id(1L).name("Sample").build();

        sampleEntity = new SampleEntity();
        sampleEntity.setId(1L);
        sampleEntity.setName("Sample");

        lenient().when(tracer.spanBuilder(anyString())).thenReturn(spanBuilder);
        lenient().when(spanBuilder.startSpan()).thenReturn(span);
        lenient().when(span.makeCurrent()).thenReturn(scope);

        lenient().when(metrics.getCreateTimer()).thenReturn(createTimer);
        lenient().when(metrics.getFindByIdTimer()).thenReturn(findByIdTimer);
        lenient().when(metrics.getFindAllTimer()).thenReturn(findAllTimer);
        lenient().when(metrics.getUpdateTimer()).thenReturn(updateTimer);
        lenient().when(metrics.getDeleteTimer()).thenReturn(deleteTimer);

        lenient().when(metrics.getCreateSuccess()).thenReturn(createSuccess);
        lenient().when(metrics.getCreateError()).thenReturn(createError);

        lenient().when(metrics.getFindSuccess()).thenReturn(findSuccess);
        lenient().when(metrics.getFindError()).thenReturn(findError);
        lenient().when(metrics.getFindNotFound()).thenReturn(findNotFound);

        lenient().when(metrics.getFindAllSuccess()).thenReturn(findAllSuccess);
        lenient().when(metrics.getFindAllError()).thenReturn(findAllError);

        lenient().when(metrics.getUpdateSuccess()).thenReturn(updateSuccess);
        lenient().when(metrics.getUpdateError()).thenReturn(updateError);
        lenient().when(metrics.getUpdateNotFound()).thenReturn(updateNotFound);

        lenient().when(metrics.getDeleteSuccess()).thenReturn(deleteSuccess);
        lenient().when(metrics.getDeleteError()).thenReturn(deleteError);
        lenient().when(metrics.getDeleteNotFound()).thenReturn(deleteNotFound);

        lenient().when(metrics.getTechnicalErrors()).thenReturn(technicalErrors);

        lenient()
                .when(createTimer.record(any(java.util.function.Supplier.class)))
                .thenAnswer(invocation -> ((java.util.function.Supplier<?>) invocation.getArgument(0)).get());

        lenient()
                .when(findByIdTimer.record(any(java.util.function.Supplier.class)))
                .thenAnswer(invocation -> ((java.util.function.Supplier<?>) invocation.getArgument(0)).get());

        lenient()
                .when(findAllTimer.record(any(java.util.function.Supplier.class)))
                .thenAnswer(invocation -> ((java.util.function.Supplier<?>) invocation.getArgument(0)).get());

        lenient()
                .when(updateTimer.record(any(java.util.function.Supplier.class)))
                .thenAnswer(invocation -> ((java.util.function.Supplier<?>) invocation.getArgument(0)).get());

        lenient()
                .doAnswer(invocation -> {
                    ((Runnable) invocation.getArgument(0)).run();
                    return null;
                })
                .when(deleteTimer)
                .record(any(Runnable.class));
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("Doit créer une entité")
        void shouldCreateEntity() {
            when(sampleMapper.toEntity(sampleDto)).thenReturn(sampleEntity);
            when(sampleRepository.save(sampleEntity)).thenReturn(sampleEntity);
            when(sampleMapper.toDto(sampleEntity)).thenReturn(sampleDto);

            SampleDto result = sampleService.create(sampleDto);

            assertEquals(sampleDto, result);
            verify(sampleMapper).toEntity(sampleDto);
            verify(sampleRepository).save(sampleEntity);
            verify(sampleMapper).toDto(sampleEntity);
            verify(sampleRepository).save(sampleEntity);
            verify(createSuccess).increment();
        }

        @Test
        @DisplayName("Doit lever une TechnicalException lorsqu'une erreur survient")
        void shouldThrowTechnicalException() {
            when(sampleMapper.toEntity(sampleDto)).thenThrow(new RuntimeException("boom"));
            assertThrows(TechnicalException.class, () -> sampleService.create(sampleDto));
            verify(sampleRepository, never()).save(any());
            verify(createError).increment();
            verify(technicalErrors).increment();
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
        }

        @Test
        @DisplayName("Doit lever une NotFoundException lorsque l'entité n'existe pas")
        void shouldThrowNotFoundExceptionWhenEntityDoesNotExist() {
            when(sampleRepository.findById(1L)).thenReturn(Optional.empty());
            assertThrows(NotFoundException.class, () -> sampleService.findById(1L));
            verify(findNotFound).increment();
        }
    }

    @Nested
    @DisplayName("findAll")
    class FindAll {

        @Test
        @DisplayName("Doit retourner toutes les entités")
        void shouldReturnAllEntities() {
            SampleEntity entity2 = new SampleEntity();
            entity2.setId(2L);

            SampleDto dto2 = SampleDto.builder().id(2L).name("Sample2").build();
            when(sampleRepository.findAll()).thenReturn(List.of(sampleEntity, entity2));
            when(sampleMapper.toDto(sampleEntity)).thenReturn(sampleDto);
            when(sampleMapper.toDto(entity2)).thenReturn(dto2);

            List<SampleDto> result = sampleService.findAll();

            assertEquals(2, result.size());
            assertEquals(sampleDto, result.get(0));
            assertEquals(dto2, result.get(1));
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("Doit mettre à jour une entité existante")
        void shouldUpdateEntity() {
            when(sampleRepository.findById(1L)).thenReturn(Optional.of(sampleEntity));
            when(sampleRepository.save(sampleEntity)).thenReturn(sampleEntity);
            when(sampleMapper.toDto(sampleEntity)).thenReturn(sampleDto);
            SampleDto result = sampleService.update(1L, sampleDto);
            assertEquals(sampleDto, result);
            verify(sampleMapper).updateEntityFromDto(sampleDto, sampleEntity);
            verify(sampleRepository).save(sampleEntity);
        }

        @Test
        @DisplayName("Doit lever une NotFoundException lorsque l'entité n'existe pas")
        void shouldThrowNotFoundExceptionWhenUpdatingMissingEntity() {
            when(sampleRepository.findById(1L)).thenReturn(Optional.empty());
            assertThrows(NotFoundException.class, () -> sampleService.update(1L, sampleDto));
            verify(updateNotFound).increment();
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
            verify(deleteNotFound).increment();
        }
    }
}
