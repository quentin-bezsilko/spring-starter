package com.qbe.springstarter.dto.bulk;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record BulkIdsDto(@NotEmpty List<Long> ids) {}
