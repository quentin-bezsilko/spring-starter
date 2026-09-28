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
        @DisplayName("Doit retourner 200 avec la liste des DTO")
        void shouldFindAllSamples() {

            List<SampleDto> dtos = List.of(
                    sampleDto, SampleDto.builder().id(2L).name("Sample2").build());

            when(sampleService.findAll()).thenReturn(dtos);
            ResponseEntity<List<SampleDto>> response = sampleController.findAll();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(2, response.getBody().size());
            verify(sampleService).findAll();
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("Doit retourner 200 avec le DTO mis à jour")
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
