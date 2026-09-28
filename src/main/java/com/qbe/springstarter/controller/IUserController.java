package com.qbe.springstarter.controller;

import com.qbe.springstarter.dto.UserDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Users", description = "Operations on users")
@RequestMapping("/api/v1/users")
public interface IUserController {

    @Operation(
            summary = "Retrieve a user by its identifier",
            description = "Returns the user corresponding to the provided identifier. Requires READ authority.")
    @ApiResponse(responseCode = "200", description = "User successfully retrieved")
    @ApiResponse(responseCode = "401", description = "Authentication required")
    @ApiResponse(responseCode = "403", description = "Missing READ authority")
    @ApiResponse(responseCode = "404", description = "User not found")
    @GetMapping("/{id}")
    ResponseEntity<UserDto> getUser(
            @Parameter(description = "Unique identifier of the user", example = "1") @PathVariable Long id);
}
