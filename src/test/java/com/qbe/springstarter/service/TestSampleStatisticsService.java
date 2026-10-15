package com.qbe.springstarter.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import com.qbe.springstarter.dto.statistics.*;
import com.qbe.springstarter.enums.Status;
import com.qbe.springstarter.repository.SampleFavoriteRepository;
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
class TestSampleStatisticsService {

    @Mock
    private SampleRepository sampleRepository;

    @Mock
    private SampleFavoriteRepository sampleFavoriteRepository;

    @InjectMocks
    private SampleStatisticsService sampleStatisticsService;

    @Nested
    @DisplayName("getStatistics")
    class GetStatistics {

        @Test
        @DisplayName("Doit calculer toutes les statistiques")
        void shouldBuildStatistics() {
            mockRepositories();

            SampleStatisticsDto result = sampleStatisticsService.getStatistics();
            assertNotNull(result);

            assertEquals(100L, result.overview().totalSamples());
            assertEquals(80L, result.overview().activeSamples());
            assertEquals(20L, result.overview().inactiveSamples());
            assertEquals(10L, result.overview().favoriteSamples());
            assertEquals(5L, result.overview().trashedSamples());

            assertEquals(2, result.statusDistribution().size());
            assertEquals(2, result.categoryDistribution().size());

            assertEquals(10L, result.favoriteDistribution().favorites());
            assertEquals(90L, result.favoriteDistribution().nonFavorites());

            assertEquals(75L, result.stockDistribution().withStock());
            assertEquals(25L, result.stockDistribution().withoutStock());

            assertEquals(5L, result.trashDistribution().trashed());
            assertEquals(95L, result.trashDistribution().active());

            assertEquals(1, result.monthlyCreations().size());
            assertEquals(
                    0, BigDecimal.valueOf(10).compareTo(result.priceStatistics().min()));
            assertEquals(
                    0,
                    BigDecimal.valueOf(200).compareTo(result.priceStatistics().max()));
            assertEquals(
                    0,
                    BigDecimal.valueOf(105.5).compareTo(result.priceStatistics().average()));
            assertEquals(
                    0,
                    BigDecimal.valueOf(1).compareTo(result.quantityStatistics().min()));
            assertEquals(
                    0,
                    BigDecimal.valueOf(50).compareTo(result.quantityStatistics().max()));
            assertEquals(
                    0,
                    BigDecimal.valueOf(25.5)
                            .compareTo(result.quantityStatistics().average()));
        }

        @Test
        @DisplayName("Doit trier les statuts par ordre alphabétique")
        void shouldSortStatusDistribution() {
            mockCommonData(50L, 12L);

            when(sampleRepository.countByStatus())
                    .thenReturn(
                            List.of(new Object[] {Status.DELETED, 1L}, new Object[] {Status.ACTIVE, 2L}, new Object[] {
                                Status.CREATED, 3L
                            }));

            SampleStatisticsDto result = sampleStatisticsService.getStatistics();
            assertEquals(
                    List.of("ACTIVE", "CREATED", "DELETED"),
                    result.statusDistribution().stream()
                            .map(StatusDistributionDto::status)
                            .toList());
        }

        @Test
        @DisplayName("Doit trier les catégories par ordre alphabétique")
        void shouldSortCategoryDistribution() {
            mockCommonData(50L, 12L);

            when(sampleRepository.countByCategory())
                    .thenReturn(List.of(new Object[] {'D', 5L}, new Object[] {'B', 4L}, new Object[] {'A', 3L}));

            SampleStatisticsDto result = sampleStatisticsService.getStatistics();
            assertEquals(
                    List.of("A", "B", "D"),
                    result.categoryDistribution().stream()
                            .map(CategoryDistributionDto::category)
                            .toList());
        }

        @Test
        @DisplayName("Doit convertir les catégories Character en String")
        void shouldConvertCharacterCategoryToString() {
            mockCommonData(50L, 12L);

            when(sampleRepository.countByCategory()).thenReturn(List.<Object[]>of(new Object[] {'A', 12L}));

            SampleStatisticsDto result = sampleStatisticsService.getStatistics();
            assertEquals("A", result.categoryDistribution().getFirst().category());
        }

        @Test
        @DisplayName("Doit retourner zéro lorsque les statistiques numériques sont null")
        void shouldHandleNullNumericValues() {
            mockCommonData(50L, 12L);

            when(sampleRepository.priceStatistics()).thenReturn(List.<Object[]>of(new Object[] {null, null, null}));
            when(sampleRepository.quantityStatistics()).thenReturn(List.<Object[]>of(new Object[] {null, null, null}));

            SampleStatisticsDto result = sampleStatisticsService.getStatistics();
            assertEquals(BigDecimal.ZERO, result.priceStatistics().min());
            assertEquals(BigDecimal.ZERO, result.priceStatistics().max());
            assertEquals(BigDecimal.ZERO, result.priceStatistics().average());

            assertEquals(BigDecimal.ZERO, result.quantityStatistics().min());
            assertEquals(BigDecimal.ZERO, result.quantityStatistics().max());
            assertEquals(BigDecimal.ZERO, result.quantityStatistics().average());
        }

        @Test
        @DisplayName("Doit calculer correctement les non favoris")
        void shouldCalculateNonFavorites() {
            mockCommonData(50L, 12L);

            SampleStatisticsDto result = sampleStatisticsService.getStatistics();
            assertEquals(12L, result.favoriteDistribution().favorites());
            assertEquals(38L, result.favoriteDistribution().nonFavorites());
        }
    }

    private void mockRepositories() {
        when(sampleRepository.count()).thenReturn(100L);
        when(sampleRepository.countByActiveTrue()).thenReturn(80L);
        when(sampleRepository.countByActiveFalse()).thenReturn(20L);
        when(sampleRepository.countByStatus(Status.DELETED)).thenReturn(5L);
        when(sampleFavoriteRepository.count()).thenReturn(10L);
        when(sampleRepository.countByStockGreaterThan(0L)).thenReturn(75L);
        when(sampleRepository.countWithoutStock()).thenReturn(25L);
        when(sampleRepository.countByStatus())
                .thenReturn(List.of(new Object[] {Status.ACTIVE, 80L}, new Object[] {Status.DELETED, 5L}));
        when(sampleRepository.countByCategory()).thenReturn(List.of(new Object[] {'B', 40L}, new Object[] {'A', 60L}));
        when(sampleRepository.countByCreationMonth()).thenReturn(List.<Object[]>of(new Object[] {"2026-10", 100L}));
        when(sampleRepository.priceStatistics())
                .thenReturn(List.<Object[]>of(new Object[] {BigDecimal.valueOf(10), BigDecimal.valueOf(200), 105.5}));
        when(sampleRepository.quantityStatistics()).thenReturn(List.<Object[]>of(new Object[] {1L, 50L, 25.5}));
    }

    private void mockCommonData(long total, long favorites) {
        when(sampleRepository.count()).thenReturn(total);
        when(sampleRepository.countByActiveTrue()).thenReturn(total);
        when(sampleRepository.countByActiveFalse()).thenReturn(0L);
        when(sampleRepository.countByStatus(Status.DELETED)).thenReturn(0L);
        when(sampleFavoriteRepository.count()).thenReturn(favorites);
        when(sampleRepository.countByStockGreaterThan(0L)).thenReturn(total);
        when(sampleRepository.countWithoutStock()).thenReturn(0L);
        when(sampleRepository.countByCreationMonth()).thenReturn(List.<Object[]>of(new Object[] {"2026-10", total}));
        when(sampleRepository.priceStatistics())
                .thenReturn(List.<Object[]>of(new Object[] {BigDecimal.ONE, BigDecimal.TEN, 5.5}));
        when(sampleRepository.quantityStatistics()).thenReturn(List.<Object[]>of(new Object[] {1L, 10L, 5.5}));
        when(sampleRepository.countByStatus()).thenReturn(List.<Object[]>of(new Object[] {Status.ACTIVE, total}));
        when(sampleRepository.countByCategory()).thenReturn(List.<Object[]>of(new Object[] {'A', total}));
    }
}
