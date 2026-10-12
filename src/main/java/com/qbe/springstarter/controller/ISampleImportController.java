package com.qbe.springstarter.controller;

import com.qbe.springstarter.dto.ImportResultDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

public interface ISampleImportController {

    @Operation(summary = "Import samples", description = "Import samples from an Excel file. Requires WRITE authority.")
    @ApiResponse(responseCode = "200", description = "Import completed")
    @ApiResponse(responseCode = "400", description = "Invalid Excel file")
    @ApiResponse(responseCode = "401", description = "Authentication required")
    @ApiResponse(responseCode = "403", description = "Missing WRITE authority")
    @PostMapping(value = "/import", consumes = "multipart/form-data")
    ResponseEntity<ImportResultDto> importSamples(@RequestParam("file") MultipartFile file);
}
