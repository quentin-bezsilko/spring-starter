package com.qbe.springstarter.service;

import com.qbe.springstarter.constants.SpringStarterConstants;
import com.qbe.springstarter.dto.SampleDto;
import com.qbe.springstarter.entity.SampleEntity;
import com.qbe.springstarter.enums.Status;
import com.qbe.springstarter.error.NotFoundException;
import com.qbe.springstarter.error.TechnicalException;
import com.qbe.springstarter.error.VersionConflictException;
import com.qbe.springstarter.mapper.SampleMapper;
import com.qbe.springstarter.repository.SampleRepository;
import com.qbe.springstarter.spec.SampleSpecification;
import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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
    public Page<SampleDto> findAll(
            String search, Status status, Character category, Boolean active, Pageable pageable) {
        try {
            var specification = buildSpecification(search, status, category, active);
            Page<SampleDto> result =
                    sampleRepository.findAll(specification, pageable).map(sampleMapper::toDto);

            log.info(
                    "SampleEntities retrieved successfully: search={}, status={}, category={}, active={}, "
                            + "page={}, size={}, totalElements={}",
                    search,
                    status,
                    category,
                    active,
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    result.getTotalElements());

            return result;
        } catch (Exception e) {
            log.error("Error during filtered findAll", e);
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

    @Override
    @Transactional
    @CachePut(value = "samples", key = "#result.id")
    public SampleDto duplicate(Long id) {
        SampleEntity entity = sampleRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException(SpringStarterConstants.SAMPLE_ENTITY_RESOURCE_NAME, id));

        SampleEntity duplicatedEntity = sampleMapper.duplicate(entity);
        duplicatedEntity.setName(entity.getName() + " (copie)");
        duplicatedEntity.setExternalId(UUID.randomUUID());
        duplicatedEntity.setStatus(Status.CREATED);
        duplicatedEntity.setCreatedAt(LocalDateTime.now());
        duplicatedEntity.setUpdatedAt(Instant.now());
        duplicatedEntity = sampleRepository.save(duplicatedEntity);
        log.info("Sample duplicated successfully sourceId={} duplicatedId={}", id, duplicatedEntity.getId());
        return sampleMapper.toDto(duplicatedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] export(String search, Status status, Character category, Boolean active) {
        try {
            Specification<SampleEntity> specification = buildSpecification(search, status, category, active);
            var samples = sampleRepository.findAll(specification);
            try (Workbook workbook = new XSSFWorkbook();
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                Sheet sheet = workbook.createSheet("Samples");
                Row headerRow = sheet.createRow(0);

                for (int i = 0; i < SpringStarterConstants.HEADERS.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(SpringStarterConstants.HEADERS[i]);
                }

                int rowIndex = 1;
                for (SampleEntity sample : samples) {

                    Row row = sheet.createRow(rowIndex++);
                    row.createCell(0).setCellValue(sample.getId() != null ? sample.getId() : 0);
                    row.createCell(1).setCellValue(sample.getName() != null ? sample.getName() : "");
                    row.createCell(2).setCellValue(sample.getDescription() != null ? sample.getDescription() : "");
                    row.createCell(3)
                            .setCellValue(
                                    sample.getStatus() != null
                                            ? sample.getStatus().name()
                                            : "");
                    row.createCell(4)
                            .setCellValue(
                                    sample.getCategory() != null
                                            ? sample.getCategory().toString()
                                            : "");
                    row.createCell(5).setCellValue(sample.getQuantity() != null ? sample.getQuantity() : 0);
                    row.createCell(6).setCellValue(sample.getStock() != null ? sample.getStock() : 0);
                    row.createCell(7).setCellValue(sample.getWeight() != null ? sample.getWeight() : 0d);
                    row.createCell(8).setCellValue(sample.getRatio() != null ? sample.getRatio() : 0f);
                    row.createCell(9)
                            .setCellValue(
                                    sample.getPrice() != null
                                            ? sample.getPrice().doubleValue()
                                            : 0d);
                    row.createCell(10).setCellValue(Boolean.TRUE.equals(sample.getActive()));
                    row.createCell(11)
                            .setCellValue(
                                    sample.getManufacturedDate() != null
                                            ? sample.getManufacturedDate().toString()
                                            : "");
                    row.createCell(12)
                            .setCellValue(
                                    sample.getManufacturedTime() != null
                                            ? sample.getManufacturedTime().toString()
                                            : "");
                    row.createCell(13).setCellValue(sample.getComments() != null ? sample.getComments() : "");
                    row.createCell(14)
                            .setCellValue(
                                    sample.getExternalId() != null
                                            ? sample.getExternalId().toString()
                                            : "");
                    row.createCell(15)
                            .setCellValue(
                                    sample.getCreatedAt() != null
                                            ? sample.getCreatedAt().toString()
                                            : "");
                    row.createCell(16)
                            .setCellValue(
                                    sample.getUpdatedAt() != null
                                            ? sample.getUpdatedAt().toString()
                                            : "");
                    row.createCell(17).setCellValue(sample.getVersion() != null ? sample.getVersion() : 0);
                }

                for (int i = 0; i < SpringStarterConstants.HEADERS.length; i++) {
                    sheet.autoSizeColumn(i);
                }

                workbook.write(outputStream);

                log.info(
                        "Samples exported successfully: search={}, status={}, category={}, active={}, count={}",
                        search,
                        status,
                        category,
                        active,
                        samples.size());

                return outputStream.toByteArray();
            }
        } catch (Exception e) {
            log.error("Error during samples export", e);
            throw new TechnicalException("Failed to export samples");
        }
    }

    private Specification<SampleEntity> buildSpecification(
            String search, Status status, Character category, Boolean active) {
        return Specification.where(SampleSpecification.search(search))
                .and(SampleSpecification.hasStatus(status))
                .and(SampleSpecification.hasCategory(category))
                .and(SampleSpecification.isActive(active));
    }
}
