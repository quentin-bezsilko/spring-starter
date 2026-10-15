package com.qbe.springstarter.dto.bulk;

import java.util.List;

public record BulkResultDto(int success, int failed, List<String> errors) {}
