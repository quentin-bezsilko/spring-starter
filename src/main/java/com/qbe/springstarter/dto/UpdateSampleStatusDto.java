package com.qbe.springstarter.dto;

import com.qbe.springstarter.enums.Status;
import jakarta.validation.constraints.NotNull;

public record UpdateSampleStatusDto(@NotNull Status status) {}
