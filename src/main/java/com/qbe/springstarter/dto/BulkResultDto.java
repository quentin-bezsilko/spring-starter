package com.qbe.springstarter.dto;

import java.util.List;

public record BulkResultDto(int success, int failed, List<String> errors) {}
