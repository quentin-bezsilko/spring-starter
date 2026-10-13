package com.qbe.springstarter.controller;

import com.qbe.springstarter.dto.*;
import com.qbe.springstarter.enums.Status;
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
    ResponseEntity<SampleDto> create(@Parameter(description = "Sample to create") @Valid @RequestBody SampleDto dto);

    @Operation(summary = "Retrieve a sample by its identifier", description = "Requires READ authority.")
    @ApiResponse(responseCode = "200", description = "Sample successfully retrieved")
    @ApiResponse(responseCode = "401", description = "Authentication required")
    @ApiResponse(responseCode = "403", description = "Missing READ authority")
    @ApiResponse(responseCode = "404", description = "Sample not found")
    @GetMapping("/{id}")
    ResponseEntity<SampleDto> findById(
            @Parameter(description = "Unique identifier of the sample", example = "1") @PathVariable Long id);

    @Operation(
            summary = "Retrieve all samples",
            description =
                    "Retrieve samples with optional search, filters, pagination and sorting. Requires READ authority.")
    @ApiResponse(responseCode = "200", description = "Samples successfully retrieved")
    @ApiResponse(responseCode = "401", description = "Authentication required")
    @ApiResponse(responseCode = "403", description = "Missing READ authority")
    @GetMapping
    ResponseEntity<Page<SampleDto>> findAll(
            @Parameter(description = "Search in sample name, description and comments", example = "sample")
                    @RequestParam(required = false)
                    String search,
            @Parameter(description = "Filter by sample status", example = "ACTIVE") @RequestParam(required = false)
                    Status status,
            @Parameter(description = "Include samples in trash", example = "false")
                    @RequestParam(defaultValue = "false")
                    Boolean includeDeleted,
            @Parameter(description = "Filter by category", example = "A") @RequestParam(required = false)
                    Character category,
            @Parameter(description = "Filter by active state", example = "true") @RequestParam(required = false)
                    Boolean active,
            @PageableDefault(size = 20, sort = "id") Pageable pageable);

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
            @Parameter(description = "Updated sample") @Valid @RequestBody SampleDto dto);

    @Operation(summary = "Delete a sample", description = "Requires WRITE authority.")
    @ApiResponse(responseCode = "204", description = "Sample successfully deleted")
    @ApiResponse(responseCode = "401", description = "Authentication required")
    @ApiResponse(responseCode = "403", description = "Missing WRITE authority")
    @ApiResponse(responseCode = "404", description = "Sample not found")
    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(
            @Parameter(description = "Unique identifier of the sample", example = "1") @PathVariable Long id);

    @Operation(summary = "Duplicate a sample", description = "Requires WRITE authority.")
    @ApiResponse(responseCode = "201", description = "Sample successfully duplicated")
    @ApiResponse(responseCode = "401", description = "Authentication required")
    @ApiResponse(responseCode = "403", description = "Missing WRITE authority")
    @ApiResponse(responseCode = "404", description = "Sample not found")
    @PostMapping("/{id}/duplicate")
    ResponseEntity<SampleDto> duplicate(
            @Parameter(description = "Unique identifier of the sample", example = "1") @PathVariable Long id);

    @Operation(
            summary = "Export samples",
            description = "Export filtered samples to an Excel file. Requires READ authority.")
    @ApiResponse(responseCode = "200", description = "Excel file successfully generated")
    @ApiResponse(responseCode = "401", description = "Authentication required")
    @ApiResponse(responseCode = "403", description = "Missing READ authority")
    @GetMapping("/export")
    ResponseEntity<byte[]> export(
            @Parameter(description = "Search in sample name, description and comments", example = "sample")
                    @RequestParam(required = false)
                    String search,
            @Parameter(description = "Filter by sample status", example = "ACTIVE") @RequestParam(required = false)
                    Status status,
            @Parameter(description = "Include samples in trash", example = "false")
                    @RequestParam(defaultValue = "false")
                    Boolean includeDeleted,
            @Parameter(description = "Filter by category", example = "A") @RequestParam(required = false)
                    Character category,
            @Parameter(description = "Filter by active state", example = "true") @RequestParam(required = false)
                    Boolean active);

    @Operation(
            summary = "Delete samples in bulk",
            description = "Delete multiple samples in a single operation. Requires WRITE authority.")
    @ApiResponse(responseCode = "200", description = "Samples successfully deleted")
    @ApiResponse(responseCode = "400", description = "Invalid request")
    @ApiResponse(responseCode = "401", description = "Authentication required")
    @ApiResponse(responseCode = "403", description = "Missing WRITE authority")
    @PostMapping("/bulk-delete")
    ResponseEntity<BulkResultDto> bulkDelete(
            @Parameter(description = "Identifiers of the samples to delete") @RequestBody BulkIdsDto request);

    @Operation(
            summary = "Duplicate samples in bulk",
            description = "Duplicate multiple samples in a single operation. Requires WRITE authority.")
    @ApiResponse(responseCode = "200", description = "Samples successfully duplicated")
    @ApiResponse(responseCode = "400", description = "Invalid request")
    @ApiResponse(responseCode = "401", description = "Authentication required")
    @ApiResponse(responseCode = "403", description = "Missing WRITE authority")
    @PostMapping("/bulk-duplicate")
    ResponseEntity<BulkResultDto> bulkDuplicate(
            @Parameter(description = "Identifiers of the samples to duplicate") @RequestBody BulkIdsDto request);

    @Operation(
            summary = "Update status of samples in bulk",
            description = "Update the status of multiple samples in a single operation. Requires WRITE authority.")
    @ApiResponse(responseCode = "200", description = "Sample statuses successfully updated")
    @ApiResponse(responseCode = "400", description = "Invalid request")
    @ApiResponse(responseCode = "401", description = "Authentication required")
    @ApiResponse(responseCode = "403", description = "Missing WRITE authority")
    @PostMapping("/bulk-status")
    ResponseEntity<BulkResultDto> bulkUpdateStatus(
            @Parameter(description = "Identifiers of the samples and target status") @Valid @RequestBody
                    BulkStatusDto request);

    @Operation(
            summary = "Update sample status",
            description = "Update only the status of a sample. Requires WRITE authority.")
    @ApiResponse(responseCode = "202", description = "Status successfully updated")
    @ApiResponse(responseCode = "400", description = "Validation error")
    @ApiResponse(responseCode = "401", description = "Authentication required")
    @ApiResponse(responseCode = "403", description = "Missing WRITE authority")
    @ApiResponse(responseCode = "404", description = "Sample not found")
    @PatchMapping("/{id}/status")
    ResponseEntity<SampleDto> updateStatus(
            @Parameter(description = "Unique identifier of the sample", example = "1") @PathVariable Long id,
            @Parameter(description = "New status") @Valid @RequestBody UpdateSampleStatusDto request);

    @Operation(
            summary = "Export selected samples",
            description = "Export the selected samples to an Excel file. Requires READ authority.")
    @ApiResponse(responseCode = "200", description = "Excel file successfully generated")
    @ApiResponse(responseCode = "400", description = "Invalid request")
    @ApiResponse(responseCode = "401", description = "Authentication required")
    @ApiResponse(responseCode = "403", description = "Missing READ authority")
    @PostMapping("/bulk-export")
    ResponseEntity<byte[]> bulkExport(
            @Parameter(description = "Identifiers of the samples to export") @RequestBody BulkIdsDto request);
}
