package com.qbe.springstarter.testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

import com.qbe.springstarter.dto.SampleDto;
import com.qbe.springstarter.dto.statistics.CategoryDistributionDto;
import com.qbe.springstarter.dto.statistics.SampleStatisticsDto;
import com.qbe.springstarter.dto.statistics.StatusDistributionDto;
import com.qbe.springstarter.enums.Status;
import com.qbe.springstarter.service.SampleService;
import com.qbe.springstarter.service.SampleStatisticsService;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;

@WithMockUser(username = "test-user")
class TestSampleStatisticsServiceIT extends TestAbstractIntegration {

    @Autowired
    private SampleStatisticsService sampleStatisticsService;

    @Autowired
    private SampleService sampleService;

    @Test
    void shouldReturnOverviewStatistics() {
        sampleService.create(buildSampleDto("STAT-OVERVIEW-1-" + UUID.randomUUID()));
        sampleService.create(buildSampleDto("STAT-OVERVIEW-2-" + UUID.randomUUID()).toBuilder()
                .active(false)
                .externalId(UUID.randomUUID())
                .build());

        SampleStatisticsDto result = sampleStatisticsService.getStatistics();
        assertThat(result).isNotNull();

        assertThat(result.overview().totalSamples()).isGreaterThanOrEqualTo(2);
        assertThat(result.overview().activeSamples()).isGreaterThanOrEqualTo(1);
        assertThat(result.overview().inactiveSamples()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void shouldReturnStatusDistribution() {
        String marker = UUID.randomUUID().toString();

        sampleService.create(buildSampleDto("ACTIVE-" + marker).toBuilder()
                .status(Status.ACTIVE)
                .build());

        sampleService.create(buildSampleDto("ARCHIVED-" + marker).toBuilder()
                .status(Status.ARCHIVED)
                .externalId(UUID.randomUUID())
                .build());

        SampleStatisticsDto result = sampleStatisticsService.getStatistics();
        assertThat(result.statusDistribution())
                .extracting(StatusDistributionDto::status)
                .contains("ACTIVE", "ARCHIVED");
    }

    @Test
    void shouldReturnCategoryDistribution() {
        String marker = UUID.randomUUID().toString();
        sampleService.create(
                buildSampleDto("CATEGORY-A-" + marker).toBuilder().category('A').build());
        sampleService.create(buildSampleDto("CATEGORY-B-" + marker).toBuilder()
                .category('B')
                .externalId(UUID.randomUUID())
                .build());

        SampleStatisticsDto result = sampleStatisticsService.getStatistics();
        assertThat(result.categoryDistribution())
                .extracting(CategoryDistributionDto::category)
                .contains("A", "B");
    }

    @Test
    void shouldReturnFavoriteStatistics() {
        SampleStatisticsDto result = sampleStatisticsService.getStatistics();
        long total = result.favoriteDistribution().favorites()
                + result.favoriteDistribution().nonFavorites();
        assertThat(total).isEqualTo(result.overview().totalSamples());
    }

    @Test
    void shouldReturnStockStatistics() {
        sampleService.create(buildSampleDto("STOCK-" + UUID.randomUUID()));

        SampleStatisticsDto result = sampleStatisticsService.getStatistics();

        long total = result.stockDistribution().withStock()
                + result.stockDistribution().withoutStock();
        assertThat(total).isEqualTo(result.overview().totalSamples());
    }

    @Test
    void shouldReturnTrashStatistics() {
        SampleDto created = sampleService.create(buildSampleDto("TRASH-" + UUID.randomUUID()));
        sampleService.updateStatus(created.id(), Status.DELETED);

        SampleStatisticsDto result = sampleStatisticsService.getStatistics();
        assertThat(result.trashDistribution().trashed()).isGreaterThanOrEqualTo(1);
        assertThat(result.trashDistribution().trashed()
                        + result.trashDistribution().active())
                .isEqualTo(result.overview().totalSamples());
    }

    @Test
    void shouldReturnMonthlyCreationStatistics() {
        sampleService.create(buildSampleDto("MONTHLY-" + UUID.randomUUID()));

        SampleStatisticsDto result = sampleStatisticsService.getStatistics();
        assertThat(result.monthlyCreations()).isNotEmpty();
        assertThat(result.monthlyCreations().getFirst().count()).isPositive();
    }

    @Test
    void shouldReturnPriceStatistics() {
        sampleService.create(buildSampleDto("PRICE-1-" + UUID.randomUUID()).toBuilder()
                .price(BigDecimal.valueOf(10))
                .build());
        sampleService.create(buildSampleDto("PRICE-2-" + UUID.randomUUID()).toBuilder()
                .price(BigDecimal.valueOf(100))
                .externalId(UUID.randomUUID())
                .build());

        SampleStatisticsDto result = sampleStatisticsService.getStatistics();
        assertThat(result.priceStatistics().min()).isNotNull();
        assertThat(result.priceStatistics().max()).isNotNull();
        assertThat(result.priceStatistics().average()).isNotNull();
        assertThat(result.priceStatistics().max())
                .isGreaterThanOrEqualTo(result.priceStatistics().min());
    }

    @Test
    void shouldReturnQuantityStatistics() {
        sampleService.create(buildSampleDto("QUANTITY-1-" + UUID.randomUUID()).toBuilder()
                .quantity(10)
                .build());
        sampleService.create(buildSampleDto("QUANTITY-2-" + UUID.randomUUID()).toBuilder()
                .quantity(100)
                .externalId(UUID.randomUUID())
                .build());

        SampleStatisticsDto result = sampleStatisticsService.getStatistics();
        assertThat(result.quantityStatistics().min()).isNotNull();
        assertThat(result.quantityStatistics().max()).isNotNull();
        assertThat(result.quantityStatistics().average()).isNotNull();
        assertThat(result.quantityStatistics().max())
                .isGreaterThanOrEqualTo(result.quantityStatistics().min());
    }

    @Test
    void shouldReturnConsistentStatistics() {
        SampleStatisticsDto result = sampleStatisticsService.getStatistics();
        assertThat(result.overview().activeSamples() + result.overview().inactiveSamples())
                .isEqualTo(result.overview().totalSamples());
        assertThat(result.favoriteDistribution().favorites()
                        + result.favoriteDistribution().nonFavorites())
                .isEqualTo(result.overview().totalSamples());
        assertThat(result.trashDistribution().trashed()
                        + result.trashDistribution().active())
                .isEqualTo(result.overview().totalSamples());
    }
}
