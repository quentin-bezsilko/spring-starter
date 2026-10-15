package com.qbe.springstarter.dto.statistics;

public record OverviewDto(
        long totalSamples, long activeSamples, long inactiveSamples, long favoriteSamples, long trashedSamples) {}
