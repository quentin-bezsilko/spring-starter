package com.qbe.springstarter.dto.statistics;

import java.util.List;

public record SampleStatisticsDto(
        OverviewDto overview,
        List<StatusDistributionDto> statusDistribution,
        List<CategoryDistributionDto> categoryDistribution,
        FavoriteDistributionDto favoriteDistribution,
        StockDistributionDto stockDistribution,
        TrashDistributionDto trashDistribution,
        List<MonthlyCreationDto> monthlyCreations,
        NumericStatisticsDto priceStatistics,
        NumericStatisticsDto quantityStatistics) {}
