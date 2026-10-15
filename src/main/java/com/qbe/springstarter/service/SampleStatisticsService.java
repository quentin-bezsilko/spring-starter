package com.qbe.springstarter.service;

import com.qbe.springstarter.dto.statistics.*;
import com.qbe.springstarter.enums.Status;
import com.qbe.springstarter.repository.SampleFavoriteRepository;
import com.qbe.springstarter.repository.SampleRepository;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SampleStatisticsService implements ISampleStatisticsService {

    private final SampleRepository sampleRepository;
    private final SampleFavoriteRepository sampleFavoriteRepository;

    @Override
    public SampleStatisticsDto getStatistics() {
        return new SampleStatisticsDto(
                loadOverview(),
                loadStatusDistribution(),
                loadCategoryDistribution(),
                loadFavoriteDistribution(),
                loadStockDistribution(),
                loadTrashDistribution(),
                loadMonthlyCreations(),
                loadPriceStatistics(),
                loadQuantityStatistics());
    }

    private OverviewDto loadOverview() {
        return new OverviewDto(
                sampleRepository.count(),
                sampleRepository.countByActiveTrue(),
                sampleRepository.countByActiveFalse(),
                sampleFavoriteRepository.count(),
                sampleRepository.countByStatus(Status.DELETED));
    }

    private List<StatusDistributionDto> loadStatusDistribution() {
        return sampleRepository.countByStatus().stream()
                .map(row -> new StatusDistributionDto(((Status) row[0]).name(), ((Number) row[1]).longValue()))
                .sorted(Comparator.comparing(StatusDistributionDto::status))
                .toList();
    }

    private List<CategoryDistributionDto> loadCategoryDistribution() {
        return sampleRepository.countByCategory().stream()
                .map(row -> new CategoryDistributionDto(row[0].toString(), ((Number) row[1]).longValue()))
                .sorted(Comparator.comparing(CategoryDistributionDto::category))
                .toList();
    }

    private FavoriteDistributionDto loadFavoriteDistribution() {
        long favorites = sampleFavoriteRepository.count();
        long nonFavorites = sampleRepository.count() - favorites;
        return new FavoriteDistributionDto(favorites, nonFavorites);
    }

    private StockDistributionDto loadStockDistribution() {
        long withStock = sampleRepository.countByStockGreaterThan(0L);
        long withoutStock = sampleRepository.countWithoutStock();
        return new StockDistributionDto(withStock, withoutStock);
    }

    private TrashDistributionDto loadTrashDistribution() {
        long trashed = sampleRepository.countByStatus(Status.DELETED);
        long active = sampleRepository.count() - trashed;
        return new TrashDistributionDto(trashed, active);
    }

    private List<MonthlyCreationDto> loadMonthlyCreations() {
        return sampleRepository.countByCreationMonth().stream()
                .map(row -> new MonthlyCreationDto(row[0].toString(), ((Number) row[1]).longValue()))
                .toList();
    }

    private NumericStatisticsDto loadPriceStatistics() {
        Object[] row = sampleRepository.priceStatistics().getFirst();
        return new NumericStatisticsDto(toBigDecimal(row[0]), toBigDecimal(row[1]), toBigDecimal(row[2]));
    }

    private NumericStatisticsDto loadQuantityStatistics() {
        Object[] row = sampleRepository.quantityStatistics().getFirst();
        return new NumericStatisticsDto(toBigDecimal(row[0]), toBigDecimal(row[1]), toBigDecimal(row[2]));
    }

    private BigDecimal toBigDecimal(Object value) {
        return value == null ? BigDecimal.ZERO : BigDecimal.valueOf(((Number) value).doubleValue());
    }
}
