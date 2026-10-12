package com.qbe.springstarter.service;

import com.qbe.springstarter.dto.ImportResultDto;
import org.springframework.web.multipart.MultipartFile;

public interface ISampleImportService {

    ImportResultDto importSamples(MultipartFile file);
}
