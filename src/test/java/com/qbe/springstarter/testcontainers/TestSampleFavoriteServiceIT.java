package com.qbe.springstarter.testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.qbe.springstarter.dto.SampleDto;
import com.qbe.springstarter.error.NotFoundException;
import com.qbe.springstarter.repository.SampleFavoriteRepository;
import com.qbe.springstarter.service.SampleFavoriteService;
import com.qbe.springstarter.service.SampleService;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;

@WithMockUser(username = "test-user")
class TestSampleFavoriteServiceIT extends TestAbstractIntegration {

    @Autowired
    private SampleFavoriteService sampleFavoriteService;

    @Autowired
    private SampleService sampleService;

    @Autowired
    private SampleFavoriteRepository sampleFavoriteRepository;

    @Test
    void shouldAddFavorite() {
        SampleDto sample = sampleService.create(buildSampleDto("FAVORITE-" + UUID.randomUUID()));
        sampleFavoriteService.addFavorite(sample.id());
        assertThat(sampleFavoriteService.isFavorite(sample.id())).isTrue();
    }

    @Test
    void shouldRemoveFavorite() {
        SampleDto sample = sampleService.create(buildSampleDto("REMOVE-FAVORITE-" + UUID.randomUUID()));

        sampleFavoriteService.addFavorite(sample.id());
        assertThat(sampleFavoriteService.isFavorite(sample.id())).isTrue();

        sampleFavoriteService.removeFavorite(sample.id());
        assertThat(sampleFavoriteService.isFavorite(sample.id())).isFalse();
    }

    @Test
    void shouldReturnFavoriteIds() {
        SampleDto sample1 = sampleService.create(buildSampleDto("FAVORITE-1-" + UUID.randomUUID()));

        SampleDto sample2 = sampleService.create(buildSampleDto("FAVORITE-2-" + UUID.randomUUID()).toBuilder()
                .externalId(UUID.randomUUID())
                .build());

        sampleFavoriteService.addFavorite(sample1.id());
        sampleFavoriteService.addFavorite(sample2.id());

        Set<Long> favorites = sampleFavoriteService.getUserFavoriteSampleIds();
        assertThat(favorites).contains(sample1.id(), sample2.id());
    }

    @Test
    void shouldReturnEmptyFavoriteIdsWhenNoFavoritesExist() {
        Set<Long> favorites = sampleFavoriteService.getUserFavoriteSampleIds();
        assertThat(favorites).isEmpty();
    }

    @Test
    void shouldAddFavoriteOnlyOnce() {
        SampleDto sample = sampleService.create(buildSampleDto("DUPLICATE-FAVORITE-" + UUID.randomUUID()));
        sampleFavoriteService.addFavorite(sample.id());
        sampleFavoriteService.addFavorite(sample.id());

        Set<Long> favorites = sampleFavoriteService.getUserFavoriteSampleIds();
        assertThat(favorites).containsExactly(sample.id());
    }

    @Test
    void shouldMarkSampleAsFavorite() {
        SampleDto sample = sampleService.create(buildSampleDto("MARK-FAVORITE-" + UUID.randomUUID()));
        assertThat(sampleFavoriteService.isFavorite(sample.id())).isFalse();

        sampleFavoriteService.addFavorite(sample.id());
        assertThat(sampleFavoriteService.isFavorite(sample.id())).isTrue();
    }

    @Test
    void shouldUnmarkSampleAsFavorite() {
        SampleDto sample = sampleService.create(buildSampleDto("UNMARK-FAVORITE-" + UUID.randomUUID()));

        sampleFavoriteService.addFavorite(sample.id());
        assertThat(sampleFavoriteService.isFavorite(sample.id())).isTrue();

        sampleFavoriteService.removeFavorite(sample.id());
        assertThat(sampleFavoriteService.isFavorite(sample.id())).isFalse();
    }

    @Test
    void shouldThrowNotFoundWhenAddingFavoriteToUnknownSample() {
        assertThatThrownBy(() -> sampleFavoriteService.addFavorite(999999L)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldThrowNotFoundWhenRemovingFavoriteFromUnknownSample() {
        assertThatThrownBy(() -> sampleFavoriteService.removeFavorite(999999L)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldPersistFavoriteInDatabase() {
        SampleDto sample = sampleService.create(buildSampleDto("PERSIST-FAVORITE-" + UUID.randomUUID()));

        sampleFavoriteService.addFavorite(sample.id());

        assertThat(sampleFavoriteRepository.findByIdUsername("test-user")).hasSize(1);
        assertThat(sampleFavoriteRepository
                        .findByIdUsername("test-user")
                        .getFirst()
                        .getId()
                        .getSampleId())
                .isEqualTo(sample.id());
    }
}
