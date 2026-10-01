package com.qbe.springstarter.service;

import com.qbe.springstarter.config.SampleMetricsConfig;
import com.qbe.springstarter.constants.SpringStarterConstants;
import com.qbe.springstarter.dto.SampleDto;
import com.qbe.springstarter.entity.SampleEntity;
import com.qbe.springstarter.error.NotFoundException;
import com.qbe.springstarter.error.TechnicalException;
import com.qbe.springstarter.error.VersionConflictException;
import com.qbe.springstarter.mapper.SampleMapper;
import com.qbe.springstarter.repository.SampleRepository;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
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
    private final Tracer tracer;
    private final SampleMetricsConfig sampleMetricsConfig;

    @Override
    @CachePut(value = "samples", key = "#result.id")
    public SampleDto create(SampleDto dto) {
        return sampleMetricsConfig.getCreateTimer().record(() -> {
            Span span = tracer.spanBuilder("sample.create").startSpan();
            try (Scope scope = span.makeCurrent()) {
                span.setAttribute("sample.name", dto.name());
                SampleEntity entity = sampleMapper.toEntity(dto);
                entity.setCreatedAt(LocalDateTime.now());
                entity.setUpdatedAt(Instant.now());
                entity = sampleRepository.save(entity);
                sampleMetricsConfig.getCreateSuccess().increment();
                return sampleMapper.toDto(entity);
            } catch (Exception e) {
                sampleMetricsConfig.getCreateError().increment();
                sampleMetricsConfig.getTechnicalErrors().increment();
                span.recordException(e);
                log.error("Error during sample creation", e);
                throw new TechnicalException("Failed to create SampleEntity");
            } finally {
                span.end();
            }
        });
    }

    @Override
    @Cacheable(value = "samples", key = "#id")
    public SampleDto findById(Long id) {
        return sampleMetricsConfig.getFindByIdTimer().record(() -> {
            try {
                return sampleRepository
                        .findById(id)
                        .map(entity -> {
                            sampleMetricsConfig.getFindSuccess().increment();
                            return sampleMapper.toDto(entity);
                        })
                        .orElseThrow(() -> {
                            sampleMetricsConfig.getFindNotFound().increment();
                            return new NotFoundException(SpringStarterConstants.SAMPLE_ENTITY_RESOURCE_NAME, id);
                        });

            } catch (NotFoundException e) {
                throw e;
            } catch (Exception e) {
                sampleMetricsConfig.getFindError().increment();
                sampleMetricsConfig.getTechnicalErrors().increment();
                log.error("Error during findById", e);
                throw e;
            }
        });
    }

    @Override
    public Page<SampleDto> findAll(Pageable pageable) {
        return sampleMetricsConfig.getFindAllTimer().record(() -> {
            try {
                Page<SampleDto> result = sampleRepository.findAll(pageable).map(sampleMapper::toDto);
                sampleMetricsConfig.getFindAllSuccess().increment();
                return result;
            } catch (Exception e) {
                sampleMetricsConfig.getFindAllError().increment();
                sampleMetricsConfig.getTechnicalErrors().increment();
                log.error("Error during paginated findAll", e);
                throw e;
            }
        });
    }

    @Override
    @Transactional
    @CachePut(value = "samples", key = "#id")
    public SampleDto update(Long id, SampleDto dto) {
        return sampleMetricsConfig.getUpdateTimer().record(() -> {
            try {
                SampleEntity entity = sampleRepository.findById(id).orElseThrow(() -> {
                    sampleMetricsConfig.getUpdateNotFound().increment();
                    return new NotFoundException(SpringStarterConstants.SAMPLE_ENTITY_RESOURCE_NAME, id);
                });
                if (!Objects.equals(entity.getVersion(), dto.version())) {
                    throw new VersionConflictException(id, dto.version(), entity.getVersion());
                }
                entity.setUpdatedAt(Instant.now());
                sampleMapper.updateEntityFromDto(dto, entity);
                entity = sampleRepository.saveAndFlush(entity);
                sampleMetricsConfig.getUpdateSuccess().increment();
                return sampleMapper.toDto(entity);
            } catch (NotFoundException | VersionConflictException e) {
                throw e;
            } catch (Exception e) {
                sampleMetricsConfig.getUpdateError().increment();
                sampleMetricsConfig.getTechnicalErrors().increment();
                log.error("Error during update", e);
                throw e;
            }
        });
    }

    @Override
    @CacheEvict(value = "samples", key = "#id")
    public void delete(Long id) {
        sampleMetricsConfig.getDeleteTimer().record(() -> {
            try {
                if (!sampleRepository.existsById(id)) {
                    sampleMetricsConfig.getDeleteNotFound().increment();
                    throw new NotFoundException(SpringStarterConstants.SAMPLE_ENTITY_RESOURCE_NAME, id);
                }
                sampleRepository.deleteById(id);
                sampleMetricsConfig.getDeleteSuccess().increment();

            } catch (NotFoundException e) {
                throw e;
            } catch (Exception e) {
                sampleMetricsConfig.getDeleteError().increment();
                sampleMetricsConfig.getTechnicalErrors().increment();
                log.error("Error during delete", e);
                throw e;
            }
        });
    }
}
