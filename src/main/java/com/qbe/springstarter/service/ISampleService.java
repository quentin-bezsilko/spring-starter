package com.qbe.springstarter.service;

import com.qbe.springstarter.dto.SampleDto;
import com.qbe.springstarter.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ISampleService {
    SampleDto create(SampleDto dto);

    SampleDto findById(Long id);

    Page<SampleDto> findAll(
            String search,
            Status status,
            Boolean includeDeleted,
            Character category,
            Boolean active,
            Pageable pageable);

    SampleDto update(Long id, SampleDto dto);

    void delete(Long id);

    SampleDto updateStatus(Long id, Status status);

    SampleDto duplicate(Long id);

    byte[] export(String search, Status status, Boolean includeDeleted, Character category, Boolean active);
}
