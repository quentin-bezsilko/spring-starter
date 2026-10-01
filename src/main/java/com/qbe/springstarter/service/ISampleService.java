package com.qbe.springstarter.service;

import com.qbe.springstarter.dto.SampleDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ISampleService {
    SampleDto create(SampleDto dto);

    SampleDto findById(Long id);

    Page<SampleDto> findAll(Pageable pageable);

    SampleDto update(Long id, SampleDto dto);

    void delete(Long id);
}
