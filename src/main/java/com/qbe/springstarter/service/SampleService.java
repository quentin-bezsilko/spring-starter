package com.qbe.springstarter.service;

import com.qbe.springstarter.constants.SpringStarterConstants;
import com.qbe.springstarter.dto.SampleDto;
import com.qbe.springstarter.entity.SampleEntity;
import com.qbe.springstarter.error.NotFoundException;
import com.qbe.springstarter.error.TechnicalException;
import com.qbe.springstarter.error.VersionConflictException;
import com.qbe.springstarter.mapper.SampleMapper;
import com.qbe.springstarter.repository.SampleRepository;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class SampleService implements ISampleService {

    private final SampleRepository sampleRepository;
    private final SampleMapper sampleMapper;

    @Override
    @CachePut(value = "samples", key = "#result.id")
    public SampleDto create(SampleDto dto) {
        try {
            SampleEntity entity = sampleMapper.toEntity(dto);
            entity.setCreatedAt(LocalDateTime.now());
            entity.setUpdatedAt(Instant.now());
            entity = sampleRepository.save(entity);

            log.info("SampleEntity created successfully with id={}", entity.getId());

            return sampleMapper.toDto(entity);
        } catch (Exception e) {
            log.error("Error during sample creation", e);
            throw new TechnicalException("Failed to create SampleEntity");
        }
    }

    @Override
    @Cacheable(value = "samples", key = "#id")
    public SampleDto findById(Long id) {
        try {
            SampleDto result = sampleRepository
                    .findById(id)
                    .map(sampleMapper::toDto)
                    .orElseThrow(() -> new NotFoundException(SpringStarterConstants.SAMPLE_ENTITY_RESOURCE_NAME, id));

            log.info("SampleEntity found successfully with id={}", id);

            return result;
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error during findById", e);
            throw e;
        }
    }

    @Override
    public Page<SampleDto> findAll(Pageable pageable) {
        try {
            Page<SampleDto> result = sampleRepository.findAll(pageable).map(sampleMapper::toDto);

            log.info(
                    "SampleEntities retrieved successfully: page={}, size={}, totalElements={}",
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    result.getTotalElements());

            return result;
        } catch (Exception e) {
            log.error("Error during paginated findAll", e);
            throw e;
        }
    }

    @Override
    @Transactional
    @CachePut(value = "samples", key = "#id")
    public SampleDto update(Long id, SampleDto dto) {
        try {
            SampleEntity entity = sampleRepository
                    .findById(id)
                    .orElseThrow(() -> new NotFoundException(SpringStarterConstants.SAMPLE_ENTITY_RESOURCE_NAME, id));

            if (!Objects.equals(entity.getVersion(), dto.version())) {
                throw new VersionConflictException(id, dto.version(), entity.getVersion());
            }

            entity.setUpdatedAt(Instant.now());
            sampleMapper.updateEntityFromDto(dto, entity);
            entity = sampleRepository.saveAndFlush(entity);

            log.info("SampleEntity updated successfully with id={}", id);
            return sampleMapper.toDto(entity);
        } catch (NotFoundException | VersionConflictException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error during update", e);
            throw e;
        }
    }

    @Override
    @CacheEvict(value = "samples", key = "#id")
    public void delete(Long id) {
        try {
            if (!sampleRepository.existsById(id)) {
                throw new NotFoundException(SpringStarterConstants.SAMPLE_ENTITY_RESOURCE_NAME, id);
            }
            sampleRepository.deleteById(id);
            log.info("SampleEntity deleted successfully with id={}", id);
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error during delete", e);
            throw e;
        }
    }
}
