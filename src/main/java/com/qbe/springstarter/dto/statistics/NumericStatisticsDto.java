package com.qbe.springstarter.dto.statistics;

import java.math.BigDecimal;

public record NumericStatisticsDto(BigDecimal min, BigDecimal max, BigDecimal average) {}
