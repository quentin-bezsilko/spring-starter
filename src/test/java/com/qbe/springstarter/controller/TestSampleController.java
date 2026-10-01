package com.qbe.springstarter.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.qbe.springstarter.dto.SampleDto;
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
        @DisplayName("Doit retourner 200 avec une page de DTO")
        void shouldFindAllSamples() {
            SampleDto secondSample = SampleDto.builder().id(2L).name("Sample2").build();

            Pageable pageable = PageRequest.of(0, 20, Sort.by("id").ascending());

            Page<SampleDto> page = new PageImpl<>(List.of(sampleDto, secondSample), pageable, 2);

            when(sampleService.findAll(pageable)).thenReturn(page);

            ResponseEntity<Page<SampleDto>> response = sampleController.findAll(pageable);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(2, response.getBody().getContent().size());
            assertEquals(2, response.getBody().getTotalElements());
            assertEquals(1, response.getBody().getTotalPages());
            assertEquals(0, response.getBody().getNumber());
            assertEquals(20, response.getBody().getSize());
            assertEquals(sampleDto, response.getBody().getContent().getFirst());
            assertEquals(secondSample, response.getBody().getContent().get(1));
            verify(sampleService).findAll(pageable);
        }

        @Test
        @DisplayName("Doit transmettre la pagination demandée au service")
        void shouldUseRequestedPagination() {
            Pageable pageable = PageRequest.of(2, 5, Sort.by("name").descending());

            Page<SampleDto> page = new PageImpl<>(List.of(), pageable, 12);
            when(sampleService.findAll(pageable)).thenReturn(page);

            ResponseEntity<Page<SampleDto>> response = sampleController.findAll(pageable);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(2, response.getBody().getNumber());
            assertEquals(5, response.getBody().getSize());
            assertEquals(12, response.getBody().getTotalElements());
            assertEquals(3, response.getBody().getTotalPages());
            verify(sampleService).findAll(pageable);
        }

        @Test
        @DisplayName("Doit retourner une page vide")
        void shouldReturnEmptyPage() {
            Pageable pageable = PageRequest.of(0, 20, Sort.by("id").ascending());

            Page<SampleDto> page = new PageImpl<>(List.of(), pageable, 0);

            when(sampleService.findAll(pageable)).thenReturn(page);

            ResponseEntity<Page<SampleDto>> response = sampleController.findAll(pageable);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().isEmpty());
            assertEquals(0, response.getBody().getTotalElements());
            verify(sampleService).findAll(pageable);
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
