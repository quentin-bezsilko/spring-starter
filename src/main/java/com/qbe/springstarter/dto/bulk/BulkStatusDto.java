package com.qbe.springstarter.dto.bulk;

import com.qbe.springstarter.enums.Status;
import java.util.List;

public record BulkStatusDto(List<Long> ids, Status status) {}
