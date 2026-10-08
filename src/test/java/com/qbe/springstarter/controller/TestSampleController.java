package com.qbe.springstarter.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.qbe.springstarter.dto.SampleDto;
import com.qbe.springstarter.enums.Status;
import com.qbe.springstarter.service.SampleService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class TestSampleController {

    @Mock
    private SampleService sampleService;

    @InjectMocks
    private SampleController sampleController;

    private SampleDto sampleDto;

    @BeforeEach
    void setUp() {
        sampleDto = SampleDto.builder().id(1L).name("Sample").build();
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("Doit retourner 201 avec le DTO créé")
        void shouldCreateSample() {
            when(sampleService.create(sampleDto)).thenReturn(sampleDto);

            ResponseEntity<SampleDto> response = sampleController.create(sampleDto);

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertEquals(sampleDto, response.getBody());

            verify(sampleService).create(sampleDto);
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("Doit retourner 200 avec le DTO trouvé")
        void shouldFindSampleById() {
            when(sampleService.findById(1L)).thenReturn(sampleDto);

            ResponseEntity<SampleDto> response = sampleController.findById(1L);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(sampleDto, response.getBody());

            verify(sampleService).findById(1L);
        }
    }

    @Nested
    @DisplayName("findAll")
    class FindAll {

        @Test
        @DisplayName("Doit retourner 200 avec une page de DTO sans filtre")
        void shouldFindAllSamplesWithoutFilters() {
            SampleDto secondSample = SampleDto.builder().id(2L).name("Sample2").build();

            Pageable pageable = PageRequest.of(0, 20, Sort.by("id").ascending());

            Page<SampleDto> page = new PageImpl<>(List.of(sampleDto, secondSample), pageable, 2);

            when(sampleService.findAll(null, null, null, null, pageable)).thenReturn(page);

            ResponseEntity<Page<SampleDto>> response = sampleController.findAll(null, null, null, null, pageable);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());

            assertEquals(2, response.getBody().getContent().size());
            assertEquals(2, response.getBody().getTotalElements());
            assertEquals(1, response.getBody().getTotalPages());
            assertEquals(0, response.getBody().getNumber());
            assertEquals(20, response.getBody().getSize());

            assertEquals(sampleDto, response.getBody().getContent().getFirst());

            assertEquals(secondSample, response.getBody().getContent().get(1));

            verify(sampleService).findAll(null, null, null, null, pageable);
        }

        @Test
        @DisplayName("Doit transmettre la pagination et le tri demandés au service")
        void shouldUseRequestedPaginationAndSort() {
            Pageable pageable = PageRequest.of(2, 5, Sort.by("name").descending());

            Page<SampleDto> page = new PageImpl<>(List.of(), pageable, 12);

            when(sampleService.findAll(null, null, null, null, pageable)).thenReturn(page);

            ResponseEntity<Page<SampleDto>> response = sampleController.findAll(null, null, null, null, pageable);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());

            assertEquals(2, response.getBody().getNumber());
            assertEquals(5, response.getBody().getSize());
            assertEquals(12, response.getBody().getTotalElements());
            assertEquals(3, response.getBody().getTotalPages());

            assertEquals(Sort.by("name").descending(), response.getBody().getSort());

            verify(sampleService).findAll(null, null, null, null, pageable);
        }

        @Test
        @DisplayName("Doit retourner une page vide")
        void shouldReturnEmptyPage() {
            Pageable pageable = PageRequest.of(0, 20, Sort.by("id").ascending());

            Page<SampleDto> page = new PageImpl<>(List.of(), pageable, 0);

            when(sampleService.findAll(null, null, null, null, pageable)).thenReturn(page);

            ResponseEntity<Page<SampleDto>> response = sampleController.findAll(null, null, null, null, pageable);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());

            assertTrue(response.getBody().isEmpty());
            assertEquals(0, response.getBody().getTotalElements());

            verify(sampleService).findAll(null, null, null, null, pageable);
        }

        @Test
        @DisplayName("Doit transmettre le critère de recherche au service")
        void shouldUseSearchFilter() {
            String search = "sample";

            Pageable pageable = PageRequest.of(0, 20, Sort.by("name").ascending());

            Page<SampleDto> page = new PageImpl<>(List.of(sampleDto), pageable, 1);

            when(sampleService.findAll(search, null, null, null, pageable)).thenReturn(page);

            ResponseEntity<Page<SampleDto>> response = sampleController.findAll(search, null, null, null, pageable);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());

            assertEquals(1, response.getBody().getTotalElements());
            assertEquals(sampleDto, response.getBody().getContent().getFirst());

            verify(sampleService).findAll(search, null, null, null, pageable);
        }

        @Test
        @DisplayName("Doit transmettre le filtre de statut au service")
        void shouldUseStatusFilter() {
            Status status = Status.ACTIVE;

            Pageable pageable = PageRequest.of(0, 20, Sort.by("id").ascending());

            Page<SampleDto> page = new PageImpl<>(List.of(sampleDto), pageable, 1);

            when(sampleService.findAll(null, status, null, null, pageable)).thenReturn(page);

            ResponseEntity<Page<SampleDto>> response = sampleController.findAll(null, status, null, null, pageable);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());

            assertEquals(1, response.getBody().getTotalElements());

            verify(sampleService).findAll(null, status, null, null, pageable);
        }

        @Test
        @DisplayName("Doit transmettre le filtre de catégorie au service")
        void shouldUseCategoryFilter() {
            Character category = 'A';

            Pageable pageable = PageRequest.of(0, 20, Sort.by("category").ascending());

            Page<SampleDto> page = new PageImpl<>(List.of(sampleDto), pageable, 1);

            when(sampleService.findAll(null, null, category, null, pageable)).thenReturn(page);

            ResponseEntity<Page<SampleDto>> response = sampleController.findAll(null, null, category, null, pageable);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());

            assertEquals(1, response.getBody().getTotalElements());

            verify(sampleService).findAll(null, null, category, null, pageable);
        }

        @Test
        @DisplayName("Doit transmettre le filtre actif au service")
        void shouldUseActiveFilter() {
            Boolean active = true;

            Pageable pageable = PageRequest.of(0, 20, Sort.by("id").ascending());

            Page<SampleDto> page = new PageImpl<>(List.of(sampleDto), pageable, 1);

            when(sampleService.findAll(null, null, null, active, pageable)).thenReturn(page);

            ResponseEntity<Page<SampleDto>> response = sampleController.findAll(null, null, null, active, pageable);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());

            assertEquals(1, response.getBody().getTotalElements());

            verify(sampleService).findAll(null, null, null, active, pageable);
        }

        @Test
        @DisplayName("Doit transmettre tous les filtres combinés au service")
        void shouldUseAllFilters() {
            String search = "sample";
            Status status = Status.ACTIVE;
            Character category = 'A';
            Boolean active = true;

            Pageable pageable = PageRequest.of(1, 8, Sort.by("price").descending());

            Page<SampleDto> page = new PageImpl<>(List.of(sampleDto), pageable, 9);

            when(sampleService.findAll(search, status, category, active, pageable))
                    .thenReturn(page);

            ResponseEntity<Page<SampleDto>> response =
                    sampleController.findAll(search, status, category, active, pageable);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());

            assertEquals(1, response.getBody().getNumber());
            assertEquals(8, response.getBody().getSize());
            assertEquals(9, response.getBody().getTotalElements());
            assertEquals(2, response.getBody().getTotalPages());

            verify(sampleService).findAll(search, status, category, active, pageable);
        }

        @Test
        @DisplayName("Doit supporter le filtre active=false")
        void shouldUseInactiveFilter() {
            Boolean active = false;

            Pageable pageable = PageRequest.of(0, 20, Sort.by("id").ascending());

            Page<SampleDto> page = new PageImpl<>(List.of(), pageable, 0);

            when(sampleService.findAll(null, null, null, active, pageable)).thenReturn(page);

            ResponseEntity<Page<SampleDto>> response = sampleController.findAll(null, null, null, active, pageable);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().isEmpty());

            verify(sampleService).findAll(null, null, null, false, pageable);
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("Doit retourner 202 avec le DTO mis à jour")
        void shouldUpdateSample() {
            when(sampleService.update(1L, sampleDto)).thenReturn(sampleDto);

            ResponseEntity<SampleDto> response = sampleController.update(1L, sampleDto);

            assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
            assertEquals(sampleDto, response.getBody());

            verify(sampleService).update(1L, sampleDto);
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("Doit retourner 204")
        void shouldDeleteSample() {
            ResponseEntity<Void> response = sampleController.delete(1L);

            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
            assertNull(response.getBody());

            verify(sampleService).delete(1L);
        }
    }
}
