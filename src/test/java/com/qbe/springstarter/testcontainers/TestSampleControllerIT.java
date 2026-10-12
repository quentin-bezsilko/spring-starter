package com.qbe.springstarter.testcontainers;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qbe.springstarter.dto.SampleDto;
import com.qbe.springstarter.enums.Status;
import java.io.ByteArrayInputStream;
import java.util.UUID;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

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
}
