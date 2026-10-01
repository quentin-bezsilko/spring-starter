package com.qbe.springstarter.controller;

import com.qbe.springstarter.dto.SampleDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    ResponseEntity<Page<SampleDto>> findAll(
            @PageableDefault(
                            page = 0,
                            size = 20,
                            sort = "id",
                            direction = org.springframework.data.domain.Sort.Direction.ASC)
                    Pageable pageable);

    @Operation(summary = "Update a sample", description = "Requires WRITE authority.")
    @ApiResponse(responseCode = "202", description = "Sample successfully updated")
    @ApiResponse(responseCode = "400", description = "Validation error")
    @ApiResponse(responseCode = "401", description = "Authentication required")
    @ApiResponse(responseCode = "403", description = "Missing WRITE authority")
    @ApiResponse(responseCode = "404", description = "Sample not found")
    @ApiResponse(responseCode = "409", description = "Version conflict")
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
