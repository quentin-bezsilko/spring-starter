package com.qbe.springstarter.dto;

import java.time.Instant;
import java.util.Map;

public record ValidationErrorResponseDto(Instant timestamp, int status, String message, Map<String, String> errors) {}
