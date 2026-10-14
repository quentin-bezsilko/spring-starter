package com.qbe.springstarter.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Sample Favorites", description = "Operations on sample favorites")
@RequestMapping("/api/v1/samples")
public interface ISampleFavoriteController {

    @Operation(
            summary = "Add sample to favorites",
            description = "Add the sample to the favorites of the current user. Requires WRITE authority.")
    @ApiResponse(responseCode = "201", description = "Favorite successfully created")
    @ApiResponse(responseCode = "401", description = "Authentication required")
    @ApiResponse(responseCode = "403", description = "Missing WRITE authority")
    @ApiResponse(responseCode = "404", description = "Sample not found")
    @PostMapping("/{id}/favorite")
    ResponseEntity<Void> addFavorite(
            @Parameter(description = "Unique identifier of the sample", example = "1") @PathVariable Long id);

    @Operation(
            summary = "Remove sample from favorites",
            description = "Remove the sample from the favorites of the current user. Requires WRITE authority.")
    @ApiResponse(responseCode = "204", description = "Favorite successfully removed")
    @ApiResponse(responseCode = "401", description = "Authentication required")
    @ApiResponse(responseCode = "403", description = "Missing WRITE authority")
    @ApiResponse(responseCode = "404", description = "Sample not found")
    @DeleteMapping("/{id}/favorite")
    ResponseEntity<Void> removeFavorite(
            @Parameter(description = "Unique identifier of the sample", example = "1") @PathVariable Long id);
}
