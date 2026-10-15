package com.qbe.springstarter.testcontainers;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qbe.springstarter.dto.SampleDto;
import com.qbe.springstarter.dto.UpdateSampleStatusDto;
import com.qbe.springstarter.dto.bulk.BulkIdsDto;
import com.qbe.springstarter.dto.bulk.BulkStatusDto;
import com.qbe.springstarter.enums.Status;
import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.UUID;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@WithMockUser(username = "test-user")
class TestSampleControllerIT extends TestAbstractIntegration {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext applicationContext;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext).build();
        objectMapper = new ObjectMapper().findAndRegisterModules();
    }

    @Test
    void shouldCreateSample() throws Exception {
        mockMvc.perform(post("/api/v1/samples")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildSampleDto("CONTROLLER-PRODUCT-001"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("CONTROLLER-PRODUCT-001"));
    }

    @Test
    void shouldReturnValidationErrorWhenNameIsInvalid() throws Exception {
        SampleDto dto =
                buildSampleDto("CONTROLLER-PRODUCT-002").toBuilder().name("A").build();

        mockMvc.perform(post("/api/v1/samples")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFindAllSamples() throws Exception {
        mockMvc.perform(post("/api/v1/samples")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildSampleDto("CONTROLLER-PRODUCT-003"))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/samples").param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").isNumber());
    }

    @Test
    void shouldReturnNotFoundForUnknownId() throws Exception {
        mockMvc.perform(get("/api/v1/samples/999999")).andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdateSample() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/v1/samples")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildSampleDto("CONTROLLER-PRODUCT-004"))))
                .andExpect(status().isCreated())
                .andReturn();

        SampleDto created = objectMapper.readValue(createResult.getResponse().getContentAsString(), SampleDto.class);

        SampleDto update = created.toBuilder()
                .name("CONTROLLER-UPDATED_PRODUCT")
                .externalId(UUID.randomUUID())
                .build();

        mockMvc.perform(put("/api/v1/samples/{id}", created.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.name").value("CONTROLLER-UPDATED_PRODUCT"));
    }

    @Test
    void shouldReturnConflictWhenUpdatingWithOutdatedVersion() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/v1/samples")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildSampleDto("CONTROLLER-VERSION-CONFLICT"))))
                .andExpect(status().isCreated())
                .andReturn();

        SampleDto created = objectMapper.readValue(createResult.getResponse().getContentAsString(), SampleDto.class);

        SampleDto firstUpdate =
                created.toBuilder().name("CONTROLLER-FIRST-UPDATE").build();

        MvcResult updateResult = mockMvc.perform(put("/api/v1/samples/{id}", created.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstUpdate)))
                .andExpect(status().isAccepted())
                .andReturn();

        SampleDto updated = objectMapper.readValue(updateResult.getResponse().getContentAsString(), SampleDto.class);

        // Vérifie au passage que Hibernate a bien incrémenté la version.
        assert updated.version() > created.version();

        // Réutilisation volontaire de l'ancienne version.
        SampleDto outdatedUpdate =
                created.toBuilder().name("CONTROLLER-OUTDATED-UPDATE").build();

        mockMvc.perform(put("/api/v1/samples/{id}", created.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(outdatedUpdate)))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldDeleteSample() throws Exception {
        String response = mockMvc.perform(post("/api/v1/samples")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildSampleDto("CONTROLLER-PRODUCT-005"))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        SampleDto created = objectMapper.readValue(response, SampleDto.class);

        mockMvc.perform(delete("/api/v1/samples/{id}", created.id())).andExpect(status().isNoContent());
    }

    @Test
    void shouldDuplicateSample() throws Exception {

        MvcResult createResult = mockMvc.perform(post("/api/v1/samples")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildSampleDto("CONTROLLER-PRODUCT-DUPLICATE"))))
                .andExpect(status().isCreated())
                .andReturn();

        SampleDto created = objectMapper.readValue(createResult.getResponse().getContentAsString(), SampleDto.class);

        MvcResult duplicateResult = mockMvc.perform(post("/api/v1/samples/{id}/duplicate", created.id()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("CONTROLLER-PRODUCT-DUPLICATE (copie)"))
                .andExpect(jsonPath("$.status").value(Status.CREATED.name()))
                .andReturn();

        SampleDto duplicated =
                objectMapper.readValue(duplicateResult.getResponse().getContentAsString(), SampleDto.class);

        assert duplicated.id() != null;
        assert !duplicated.id().equals(created.id());

        assert duplicated.externalId() != null;
        assert !duplicated.externalId().equals(created.externalId());
    }

    @Test
    void shouldReturnNotFoundWhenDuplicatingUnknownSample() throws Exception {
        mockMvc.perform(post("/api/v1/samples/{id}/duplicate", 999999L)).andExpect(status().isNotFound());
    }

    @Test
    void shouldExportSamples() throws Exception {
        mockMvc.perform(post("/api/v1/samples")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildSampleDto("CONTROLLER-EXPORT-001"))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/samples/export"))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.CONTENT_DISPOSITION))
                .andExpect(header().string(
                                HttpHeaders.CONTENT_TYPE,
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    }

    @Test
    void shouldExportSamplesWithFilters() throws Exception {
        mockMvc.perform(post("/api/v1/samples")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildSampleDto("ACTIVE-EXPORT-SAMPLE"))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/samples/export").param("search", "ACTIVE").param("status", Status.ACTIVE.name()))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.CONTENT_DISPOSITION));
    }

    @Test
    void shouldReturnExcelFileContent() throws Exception {
        mockMvc.perform(post("/api/v1/samples")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildSampleDto("CONTROLLER-EXPORT-CONTENT"))))
                .andExpect(status().isCreated());

        MvcResult result = mockMvc.perform(get("/api/v1/samples/export"))
                .andExpect(status().isOk())
                .andReturn();

        byte[] content = result.getResponse().getContentAsByteArray();

        assertNotNull(content);
        assertNotEquals(0, content.length);
    }

    @Test
    void shouldGenerateValidExcelWorkbook() throws Exception {
        mockMvc.perform(post("/api/v1/samples")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildSampleDto("CONTROLLER-EXPORT-WORKBOOK"))))
                .andExpect(status().isCreated());

        MvcResult result = mockMvc.perform(get("/api/v1/samples/export"))
                .andExpect(status().isOk())
                .andReturn();

        try (Workbook workbook =
                new XSSFWorkbook(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()))) {
            assertEquals("Samples", workbook.getSheetAt(0).getSheetName());
            assertEquals("Id", workbook.getSheetAt(0).getRow(0).getCell(0).getStringCellValue());
            assertEquals("Nom", workbook.getSheetAt(0).getRow(0).getCell(1).getStringCellValue());
        }
    }

    @Test
    void shouldBulkDeleteSamples() throws Exception {

        SampleDto sample1 = objectMapper.readValue(
                mockMvc.perform(post("/api/v1/samples")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(buildSampleDto("BULK-DELETE-1"))))
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString(),
                SampleDto.class);

        SampleDto sample2 = objectMapper.readValue(
                mockMvc.perform(post("/api/v1/samples")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(buildSampleDto("BULK-DELETE-2"))))
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString(),
                SampleDto.class);

        BulkIdsDto request = new BulkIdsDto(List.of(sample1.id(), sample2.id()));

        mockMvc.perform(post("/api/v1/samples/bulk-delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(2))
                .andExpect(jsonPath("$.failed").value(0));

        mockMvc.perform(get("/api/v1/samples/{id}", sample1.id())).andExpect(status().isNotFound());

        mockMvc.perform(get("/api/v1/samples/{id}", sample2.id())).andExpect(status().isNotFound());
    }

    @Test
    void shouldBulkDuplicateSamples() throws Exception {

        SampleDto sample1 = objectMapper.readValue(
                mockMvc.perform(post("/api/v1/samples")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(buildSampleDto("BULK-DUPLICATE-1"))))
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString(),
                SampleDto.class);

        SampleDto sample2 = objectMapper.readValue(
                mockMvc.perform(post("/api/v1/samples")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(buildSampleDto("BULK-DUPLICATE-2"))))
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString(),
                SampleDto.class);

        BulkIdsDto request = new BulkIdsDto(List.of(sample1.id(), sample2.id()));

        mockMvc.perform(post("/api/v1/samples/bulk-duplicate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(2))
                .andExpect(jsonPath("$.failed").value(0));

        mockMvc.perform(get("/api/v1/samples").param("search", "(copie)")).andExpect(status().isOk());
    }

    @Test
    void shouldBulkUpdateStatus() throws Exception {

        SampleDto sample1 = objectMapper.readValue(
                mockMvc.perform(post("/api/v1/samples")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(buildSampleDto("BULK-STATUS-1"))))
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString(),
                SampleDto.class);

        SampleDto sample2 = objectMapper.readValue(
                mockMvc.perform(post("/api/v1/samples")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(buildSampleDto("BULK-STATUS-2"))))
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString(),
                SampleDto.class);

        BulkStatusDto request = new BulkStatusDto(List.of(sample1.id(), sample2.id()), Status.ARCHIVED);

        mockMvc.perform(post("/api/v1/samples/bulk-status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(2))
                .andExpect(jsonPath("$.failed").value(0));

        mockMvc.perform(get("/api/v1/samples/{id}", sample1.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(Status.ARCHIVED.name()));

        mockMvc.perform(get("/api/v1/samples/{id}", sample2.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(Status.ARCHIVED.name()));
    }

    @Test
    void shouldBulkExportSamples() throws Exception {

        SampleDto sample1 = objectMapper.readValue(
                mockMvc.perform(post("/api/v1/samples")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(buildSampleDto("BULK-EXPORT-1"))))
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString(),
                SampleDto.class);

        SampleDto sample2 = objectMapper.readValue(
                mockMvc.perform(post("/api/v1/samples")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(buildSampleDto("BULK-EXPORT-2"))))
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString(),
                SampleDto.class);

        BulkIdsDto request = new BulkIdsDto(List.of(sample1.id(), sample2.id()));

        mockMvc.perform(post("/api/v1/samples/bulk-export")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.CONTENT_DISPOSITION))
                .andExpect(header().string(
                                HttpHeaders.CONTENT_TYPE,
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    }

    @Test
    void shouldGenerateValidExcelWorkbookForBulkExport() throws Exception {

        SampleDto sample = objectMapper.readValue(
                mockMvc.perform(post("/api/v1/samples")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(buildSampleDto("BULK-EXPORT-WORKBOOK"))))
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString(),
                SampleDto.class);

        BulkIdsDto request = new BulkIdsDto(List.of(sample.id()));

        MvcResult result = mockMvc.perform(post("/api/v1/samples/bulk-export")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        try (Workbook workbook =
                new XSSFWorkbook(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()))) {

            assertEquals("Samples", workbook.getSheetAt(0).getSheetName());

            assertEquals("Id", workbook.getSheetAt(0).getRow(0).getCell(0).getStringCellValue());
        }
    }

    @Test
    void shouldUpdateSampleStatus() throws Exception {

        MvcResult createResult = mockMvc.perform(post("/api/v1/samples")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildSampleDto("PATCH-STATUS-ARCHIVED"))))
                .andExpect(status().isCreated())
                .andReturn();

        SampleDto created = objectMapper.readValue(createResult.getResponse().getContentAsString(), SampleDto.class);

        UpdateSampleStatusDto request = new UpdateSampleStatusDto(Status.ARCHIVED);

        mockMvc.perform(patch("/api/v1/samples/{id}/status", created.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id").value(created.id()))
                .andExpect(jsonPath("$.status").value(Status.ARCHIVED.name()));
    }

    @Test
    void shouldUpdateStatusToActive() throws Exception {

        MvcResult createResult = mockMvc.perform(post("/api/v1/samples")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildSampleDto("PATCH-STATUS-ACTIVE"))))
                .andExpect(status().isCreated())
                .andReturn();

        SampleDto created = objectMapper.readValue(createResult.getResponse().getContentAsString(), SampleDto.class);

        UpdateSampleStatusDto request = new UpdateSampleStatusDto(Status.ACTIVE);

        mockMvc.perform(patch("/api/v1/samples/{id}/status", created.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value(Status.ACTIVE.name()));
    }

    @Test
    void shouldUpdateStatusToDeleted() throws Exception {

        MvcResult createResult = mockMvc.perform(post("/api/v1/samples")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildSampleDto("PATCH-STATUS-DELETED"))))
                .andExpect(status().isCreated())
                .andReturn();

        SampleDto created = objectMapper.readValue(createResult.getResponse().getContentAsString(), SampleDto.class);

        UpdateSampleStatusDto request = new UpdateSampleStatusDto(Status.DELETED);

        mockMvc.perform(patch("/api/v1/samples/{id}/status", created.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value(Status.DELETED.name()));

        mockMvc.perform(get("/api/v1/samples/{id}", created.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(Status.DELETED.name()));
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingStatusOfUnknownSample() throws Exception {

        UpdateSampleStatusDto request = new UpdateSampleStatusDto(Status.ARCHIVED);

        mockMvc.perform(patch("/api/v1/samples/{id}/status", 999999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnBadRequestWhenStatusIsNull() throws Exception {

        String payload = """
        {
          "status": null
        }
        """;

        mockMvc.perform(patch("/api/v1/samples/{id}/status", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }
}
