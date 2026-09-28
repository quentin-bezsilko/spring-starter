package com.qbe.springstarter.controller;

import com.qbe.springstarter.dto.SampleDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Samples", description = "Operations on sample resources")
@RequestMapping("/api/v1/samples")
public interface ISampleController {

    @Operation(summary = "Create a sample", description = "Requires WRITE authority.")
    @ApiResponse(responseCode = "201", description = "Sample successfully created")
    @ApiResponse(responseCode = "400", description = "Validation error")
    @ApiResponse(responseCode = "401", description = "Authentication required")
    @ApiResponse(responseCode = "403", description = "Missing WRITE authority")
    @PostMapping
    ResponseEntity<SampleDto> create(@Valid @RequestBody SampleDto dto);

    @Operation(summary = "Retrieve a sample by its identifier", description = "Requires READ authority.")
    @ApiResponse(responseCode = "200", description = "Sample successfully retrieved")
    @ApiResponse(responseCode = "401", description = "Authentication required")
    @ApiResponse(responseCode = "403", description = "Missing READ authority")
    @ApiResponse(responseCode = "404", description = "Sample not found")
    @GetMapping("/{id}")
    ResponseEntity<SampleDto> findById(
            @Parameter(description = "Unique identifier of the sample", example = "1") @PathVariable Long id);

    @Operation(summary = "Retrieve all samples", description = "Requires READ authority.")
    @ApiResponse(responseCode = "200", description = "Samples successfully retrieved")
    @ApiResponse(responseCode = "401", description = "Authentication required")
    @ApiResponse(responseCode = "403", description = "Missing READ authority")
    @GetMapping
    ResponseEntity<List<SampleDto>> findAll();

    @Operation(summary = "Update a sample", description = "Requires WRITE authority.")
    @ApiResponse(responseCode = "202", description = "Sample successfully updated")
    @ApiResponse(responseCode = "400", description = "Validation error")
    @ApiResponse(responseCode = "401", description = "Authentication required")
    @ApiResponse(responseCode = "403", description = "Missing WRITE authority")
    @ApiResponse(responseCode = "404", description = "Sample not found")
    @PutMapping("/{id}")
    ResponseEntity<SampleDto> update(
            @Parameter(description = "Unique identifier of the sample", example = "1") @PathVariable Long id,
            @Valid @RequestBody SampleDto dto);

    @Operation(summary = "Delete a sample", description = "Requires WRITE authority.")
    @ApiResponse(responseCode = "204", description = "Sample successfully deleted")
    @ApiResponse(responseCode = "401", description = "Authentication required")
    @ApiResponse(responseCode = "403", description = "Missing WRITE authority")
    @ApiResponse(responseCode = "404", description = "Sample not found")
    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(
            @Parameter(description = "Unique identifier of the sample", example = "1") @PathVariable Long id);
}
