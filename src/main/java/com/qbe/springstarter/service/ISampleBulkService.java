package com.qbe.springstarter.service;

import com.qbe.springstarter.dto.BulkResultDto;
import com.qbe.springstarter.enums.Status;
import java.util.List;

public interface ISampleBulkService {

    BulkResultDto bulkDelete(List<Long> ids);

    BulkResultDto bulkDuplicate(List<Long> ids);

    BulkResultDto bulkUpdateStatus(List<Long> ids, Status status);

    byte[] bulkExport(List<Long> ids);
}
