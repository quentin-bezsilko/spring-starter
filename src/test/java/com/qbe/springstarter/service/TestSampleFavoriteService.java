package com.qbe.springstarter.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.qbe.springstarter.entity.SampleFavoriteEntity;
import com.qbe.springstarter.error.NotFoundException;
import com.qbe.springstarter.id.SampleFavoriteId;
import com.qbe.springstarter.repository.SampleFavoriteRepository;
import com.qbe.springstarter.repository.SampleRepository;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class TestSampleFavoriteService {

    @Mock
    private SampleRepository sampleRepository;

    @Mock
    private SampleFavoriteRepository sampleFavoriteRepository;

    private SampleFavoriteService sampleFavoriteService;

    private static final Long SAMPLE_ID = 1L;
    private static final String USERNAME = "test-user";

    @AfterEach
    void cleanSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private void authenticate() {
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(USERNAME, "password"));
    }

    private SampleFavoriteService buildService() {
        return new SampleFavoriteService(sampleRepository, sampleFavoriteRepository);
    }

    @Nested
    @DisplayName("addFavorite")
    class AddFavorite {

        @Test
        @DisplayName("Doit ajouter un favori")
        void shouldAddFavorite() {
            authenticate();

            sampleFavoriteService = buildService();

            when(sampleRepository.existsById(SAMPLE_ID)).thenReturn(true);
            when(sampleFavoriteRepository.existsById(any())).thenReturn(false);

            sampleFavoriteService.addFavorite(SAMPLE_ID);

            ArgumentCaptor<SampleFavoriteEntity> captor = ArgumentCaptor.forClass(SampleFavoriteEntity.class);

            verify(sampleFavoriteRepository).save(captor.capture());

            SampleFavoriteEntity saved = captor.getValue();
            assertEquals(SAMPLE_ID, saved.getId().getSampleId());
            assertEquals(USERNAME, saved.getId().getUsername());
            assertNotNull(saved.getCreatedAt());
        }

        @Test
        @DisplayName("Ne doit rien faire si le favori existe déjà")
        void shouldDoNothingWhenFavoriteAlreadyExists() {
            authenticate();

            sampleFavoriteService = buildService();

            when(sampleRepository.existsById(SAMPLE_ID)).thenReturn(true);
            when(sampleFavoriteRepository.existsById(any())).thenReturn(true);

            sampleFavoriteService.addFavorite(SAMPLE_ID);
            verify(sampleFavoriteRepository, never()).save(any());
        }

        @Test
        @DisplayName("Doit lever une NotFoundException si le sample n'existe pas")
        void shouldThrowNotFoundException() {
            authenticate();

            sampleFavoriteService = buildService();

            when(sampleRepository.existsById(SAMPLE_ID)).thenReturn(false);

            assertThrows(NotFoundException.class, () -> sampleFavoriteService.addFavorite(SAMPLE_ID));
            verify(sampleFavoriteRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("removeFavorite")
    class RemoveFavorite {

        @Test
        @DisplayName("Doit supprimer un favori")
        void shouldRemoveFavorite() {
            authenticate();

            sampleFavoriteService = buildService();

            when(sampleRepository.existsById(SAMPLE_ID)).thenReturn(true);

            sampleFavoriteService.removeFavorite(SAMPLE_ID);
            verify(sampleFavoriteRepository).deleteById(new SampleFavoriteId(SAMPLE_ID, USERNAME));
        }

        @Test
        @DisplayName("Doit lever une NotFoundException si le sample n'existe pas")
        void shouldThrowNotFoundException() {
            authenticate();

            sampleFavoriteService = buildService();
            when(sampleRepository.existsById(SAMPLE_ID)).thenReturn(false);

            assertThrows(NotFoundException.class, () -> sampleFavoriteService.removeFavorite(SAMPLE_ID));
            verify(sampleFavoriteRepository, never()).deleteById(any());
        }
    }

    @Nested
    @DisplayName("isFavorite")
    class IsFavorite {

        @Test
        @DisplayName("Doit retourner true lorsque le sample est favori")
        void shouldReturnTrue() {
            authenticate();

            sampleFavoriteService = buildService();

            when(sampleFavoriteRepository.existsById(new SampleFavoriteId(SAMPLE_ID, USERNAME)))
                    .thenReturn(true);

            boolean result = sampleFavoriteService.isFavorite(SAMPLE_ID);
            assertTrue(result);
        }

        @Test
        @DisplayName("Doit retourner false lorsque le sample n'est pas favori")
        void shouldReturnFalse() {

            authenticate();

            sampleFavoriteService = buildService();

            when(sampleFavoriteRepository.existsById(new SampleFavoriteId(SAMPLE_ID, USERNAME)))
                    .thenReturn(false);

            boolean result = sampleFavoriteService.isFavorite(SAMPLE_ID);

            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("getUserFavoriteSampleIds")
    class GetUserFavoriteSampleIds {

        @Test
        @DisplayName("Doit retourner les identifiants favoris de l'utilisateur")
        void shouldReturnFavoriteIds() {

            authenticate();

            sampleFavoriteService = buildService();

            SampleFavoriteEntity favorite1 = new SampleFavoriteEntity();
            favorite1.setId(new SampleFavoriteId(1L, USERNAME));
            favorite1.setCreatedAt(Instant.now());

            SampleFavoriteEntity favorite2 = new SampleFavoriteEntity();
            favorite2.setId(new SampleFavoriteId(2L, USERNAME));
            favorite2.setCreatedAt(Instant.now());

            when(sampleFavoriteRepository.findByIdUsername(USERNAME)).thenReturn(List.of(favorite1, favorite2));

            Set<Long> result = sampleFavoriteService.getUserFavoriteSampleIds();

            assertEquals(Set.of(1L, 2L), result);
        }

        @Test
        @DisplayName("Doit retourner un ensemble vide lorsqu'il n'y a aucun favori")
        void shouldReturnEmptySet() {

            authenticate();

            sampleFavoriteService = buildService();

            when(sampleFavoriteRepository.findByIdUsername(USERNAME)).thenReturn(List.of());

            Set<Long> result = sampleFavoriteService.getUserFavoriteSampleIds();

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("authentication")
    class Authentication {

        @Test
        @DisplayName("Doit lever une IllegalStateException sans utilisateur authentifié")
        void shouldThrowWhenNoAuthenticatedUser() {

            sampleFavoriteService = buildService();

            assertThrows(IllegalStateException.class, () -> sampleFavoriteService.isFavorite(SAMPLE_ID));
        }
    }
}
