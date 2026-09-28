package com.qbe.springstarter.error;

import java.time.Instant;
import java.util.List;
import lombok.Builder;

@Builder
public record ApiError(Instant timestamp, String code, String message, String path, List<ApiSubError> details) {}
