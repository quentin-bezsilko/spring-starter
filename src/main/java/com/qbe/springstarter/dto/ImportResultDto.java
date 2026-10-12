package com.qbe.springstarter.dto;

import java.util.List;

public record ImportResultDto(int created, int failed, List<ImportErrorDto> errors) {}
