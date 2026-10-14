package com.qbe.springstarter.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.qbe.springstarter.service.ISampleFavoriteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class TestSampleFavoriteController {

    @Mock
    private ISampleFavoriteService sampleFavoriteService;

    private SampleFavoriteController controller;

    @BeforeEach
    void setUp() {
        controller = new SampleFavoriteController(sampleFavoriteService);
    }

    @Nested
    @DisplayName("addFavorite")
    class AddFavorite {

        @Test
        @DisplayName("Doit ajouter un favori et retourner HTTP 201")
        void shouldAddFavorite() {
            Long sampleId = 1L;

            ResponseEntity<Void> response = controller.addFavorite(sampleId);
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertNull(response.getBody());

            verify(sampleFavoriteService).addFavorite(sampleId);
            verifyNoMoreInteractions(sampleFavoriteService);
        }
    }

    @Nested
    @DisplayName("removeFavorite")
    class RemoveFavorite {

        @Test
        @DisplayName("Doit supprimer un favori et retourner HTTP 204")
        void shouldRemoveFavorite() {
            Long sampleId = 1L;

            ResponseEntity<Void> response = controller.removeFavorite(sampleId);
            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
            assertNull(response.getBody());

            verify(sampleFavoriteService).removeFavorite(sampleId);
            verifyNoMoreInteractions(sampleFavoriteService);
        }
    }
}
