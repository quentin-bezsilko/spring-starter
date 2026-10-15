package com.qbe.springstarter.controller;

import com.qbe.springstarter.dto.statistics.SampleStatisticsDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Statistics", description = "Describes sample statistics")
@RequestMapping("/api/v1/samples")
public interface ISampleStatisticsController {

    @Operation(
            summary = "Get sample statistics",
            description = "Returns aggregated statistics used by the dashboard. Requires READ authority.")
    @ApiResponse(responseCode = "200", description = "Statistics successfully retrieved")
    @ApiResponse(responseCode = "401", description = "Authentication required")
    @ApiResponse(responseCode = "403", description = "Missing READ authority")
    @GetMapping("/statistics")
    ResponseEntity<SampleStatisticsDto> getStatistics();
}
