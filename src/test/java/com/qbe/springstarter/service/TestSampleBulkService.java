package com.qbe.springstarter.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.qbe.springstarter.dto.BulkResultDto;
import com.qbe.springstarter.dto.SampleDto;
import com.qbe.springstarter.entity.SampleEntity;
import com.qbe.springstarter.enums.Status;
import com.qbe.springstarter.repository.SampleRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TestSampleBulkService {

    @Mock
    private SampleService sampleService;

    @Mock
    private SampleRepository sampleRepository;

    @InjectMocks
    private SampleBulkService sampleBulkService;

    @Nested
    @DisplayName("bulkDelete")
    class BulkDelete {

        @Test
        @DisplayName("Doit supprimer tous les samples")
        void shouldDeleteAllSamples() {
            List<Long> ids = List.of(1L, 2L, 3L);
            BulkResultDto result = sampleBulkService.bulkDelete(ids);

            assertBulkResult(result, 3, 0);
            assertTrue(result.errors().isEmpty());

            verify(sampleService).delete(1L);
            verify(sampleService).delete(2L);
            verify(sampleService).delete(3L);
        }

        @Test
        @DisplayName("Doit gérer les erreurs de suppression")
        void shouldHandleDeleteErrors() {
            doAnswer(invocation -> {
                        Long id = invocation.getArgument(0);
                        if (id.equals(2L)) {
                            throw new RuntimeException("boom");
                        }
                        return null;
                    })
                    .when(sampleService)
                    .delete(anyLong());

            BulkResultDto result = sampleBulkService.bulkDelete(List.of(1L, 2L, 3L));
            assertBulkResult(result, 2, 1);
            assertEquals(1, result.errors().size());
            assertEquals(List.of("Sample 2 : boom"), result.errors());

            verify(sampleService).delete(1L);
            verify(sampleService).delete(2L);
            verify(sampleService).delete(3L);
        }
    }

    @Nested
    @DisplayName("bulkDuplicate")
    class BulkDuplicate {

        @Test
        @DisplayName("Doit dupliquer tous les samples")
        void shouldDuplicateAllSamples() {
            BulkResultDto result = sampleBulkService.bulkDuplicate(List.of(1L, 2L));
            assertBulkResult(result, 2, 0);

            verify(sampleService).duplicate(1L);
            verify(sampleService).duplicate(2L);
        }

        @Test
        @DisplayName("Doit gérer les erreurs de duplication")
        void shouldHandleDuplicateErrors() {
            doThrow(new RuntimeException("boom")).when(sampleService).duplicate(1L);
            BulkResultDto result = sampleBulkService.bulkDuplicate(List.of(1L, 2L));

            assertBulkResult(result, 1, 1);
            assertEquals(List.of("Sample 1 : boom"), result.errors());
        }
    }

    @Nested
    @DisplayName("bulkUpdateStatus")
    class BulkUpdateStatus {

        @Test
        @DisplayName("Doit mettre à jour le statut de tous les samples")
        void shouldUpdateAllStatuses() {
            SampleDto dto = SampleDto.builder().id(1L).status(Status.CREATED).build();

            when(sampleService.findById(anyLong())).thenReturn(dto);

            BulkResultDto result = sampleBulkService.bulkUpdateStatus(List.of(1L, 2L), Status.ACTIVE);
            assertBulkResult(result, 2, 0);

            verify(sampleService).findById(1L);
            verify(sampleService).findById(2L);
            verify(sampleService).update(eq(1L), any(SampleDto.class));
            verify(sampleService).update(eq(2L), any(SampleDto.class));
        }

        @Test
        @DisplayName("Doit gérer les erreurs de mise à jour")
        void shouldHandleStatusUpdateErrors() {
            when(sampleService.findById(1L)).thenThrow(new RuntimeException("boom"));

            SampleDto dto = SampleDto.builder().id(2L).status(Status.CREATED).build();
            when(sampleService.findById(2L)).thenReturn(dto);

            BulkResultDto result = sampleBulkService.bulkUpdateStatus(List.of(1L, 2L), Status.ACTIVE);
            assertBulkResult(result, 1, 1);
            assertEquals(List.of("Sample 1 : boom"), result.errors());
        }
    }

    @Nested
    @DisplayName("bulkExport")
    class BulkExport {

        @Test
        @DisplayName("Doit exporter les samples")
        void shouldExportSamples() {
            SampleEntity entity = new SampleEntity();
            entity.setId(1L);
            entity.setName("Sample");
            entity.setDescription("Description");
            entity.setStatus(Status.ACTIVE);
            entity.setCategory('A');
            entity.setQuantity(10);
            entity.setStock(100L);
            entity.setWeight(2.5d);
            entity.setRatio(1.2f);
            entity.setPrice(BigDecimal.TEN);
            entity.setActive(true);

            when(sampleRepository.findAllById(List.of(1L))).thenReturn(List.of(entity));

            byte[] result = sampleBulkService.bulkExport(List.of(1L));

            assertNotNull(result);
            assertTrue(result.length > 0);

            verify(sampleRepository).findAllById(List.of(1L));
        }

        @Test
        @DisplayName("Doit exporter une liste vide")
        void shouldExportEmptyList() {
            when(sampleRepository.findAllById(any())).thenReturn(List.of());

            byte[] result = sampleBulkService.bulkExport(List.of());
            assertNotNull(result);
            assertTrue(result.length > 0);
        }
    }

    private void assertBulkResult(BulkResultDto result, int success, int failed) {
        assertEquals(success, result.success());
        assertEquals(failed, result.failed());
    }
}
