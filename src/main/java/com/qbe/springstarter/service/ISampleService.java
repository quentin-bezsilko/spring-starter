package com.qbe.springstarter.service;

import com.qbe.springstarter.dto.SampleDto;
import java.util.List;

public interface ISampleService {
    SampleDto create(SampleDto dto);

    SampleDto findById(Long id);

    List<SampleDto> findAll();

    SampleDto update(Long id, SampleDto dto);

    void delete(Long id);
}
