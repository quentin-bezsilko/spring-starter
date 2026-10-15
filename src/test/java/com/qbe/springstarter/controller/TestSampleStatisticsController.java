package com.qbe.springstarter.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.qbe.springstarter.dto.statistics.CategoryDistributionDto;
import com.qbe.springstarter.dto.statistics.FavoriteDistributionDto;
import com.qbe.springstarter.dto.statistics.MonthlyCreationDto;
import com.qbe.springstarter.dto.statistics.NumericStatisticsDto;
import com.qbe.springstarter.dto.statistics.OverviewDto;
import com.qbe.springstarter.dto.statistics.SampleStatisticsDto;
import com.qbe.springstarter.dto.statistics.StatusDistributionDto;
import com.qbe.springstarter.dto.statistics.StockDistributionDto;
import com.qbe.springstarter.dto.statistics.TrashDistributionDto;
import com.qbe.springstarter.service.ISampleStatisticsService;
import java.math.BigDecimal;
import java.util.List;
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
class TestSampleStatisticsController {

    @Mock
    private ISampleStatisticsService sampleStatisticsService;

    @InjectMocks
    private SampleStatisticsController sampleStatisticsController;

    @Nested
    @DisplayName("getStatistics")
    class GetStatistics {

        @Test
        @DisplayName("Doit retourner 200 avec les statistiques")
        void shouldReturnStatistics() {

            SampleStatisticsDto statistics = buildStatistics();

            when(sampleStatisticsService.getStatistics()).thenReturn(statistics);

            ResponseEntity<SampleStatisticsDto> response = sampleStatisticsController.getStatistics();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(statistics, response.getBody());

            verify(sampleStatisticsService).getStatistics();
        }

        @Test
        @DisplayName("Doit retourner les données fournies par le service")
        void shouldReturnServiceResponse() {

            SampleStatisticsDto statistics = buildStatistics();

            when(sampleStatisticsService.getStatistics()).thenReturn(statistics);

            ResponseEntity<SampleStatisticsDto> response = sampleStatisticsController.getStatistics();

            assertNotNull(response.getBody());

            assertEquals(50, response.getBody().overview().totalSamples());

            assertEquals(1, response.getBody().statusDistribution().size());

            assertEquals(1, response.getBody().categoryDistribution().size());

            verify(sampleStatisticsService).getStatistics();
        }
    }

    private SampleStatisticsDto buildStatistics() {

        return new SampleStatisticsDto(
                new OverviewDto(50, 38, 12, 5, 2),
                List.of(new StatusDistributionDto("ACTIVE", 38)),
                List.of(new CategoryDistributionDto("A", 20)),
                new FavoriteDistributionDto(5, 45),
                new StockDistributionDto(40, 10),
                new TrashDistributionDto(2, 48),
                List.of(new MonthlyCreationDto("2026-10", 50)),
                new NumericStatisticsDto(BigDecimal.ONE, BigDecimal.TEN, BigDecimal.valueOf(5.5)),
                new NumericStatisticsDto(BigDecimal.valueOf(10), BigDecimal.valueOf(500), BigDecimal.valueOf(255)));
    }
}
