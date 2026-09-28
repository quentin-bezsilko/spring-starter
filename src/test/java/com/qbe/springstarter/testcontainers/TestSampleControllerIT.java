package com.qbe.springstarter.testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qbe.springstarter.dto.SampleDto;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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

        mockMvc.perform(get("/api/v1/samples")).andExpect(status().isOk());
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

        SampleDto update = buildSampleDto("CONTROLLER-UPDATED_PRODUCT").toBuilder()
                .externalId(UUID.randomUUID())
                .build();

        mockMvc.perform(put("/api/v1/samples/{id}", created.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.name").value("CONTROLLER-UPDATED_PRODUCT"));
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
}
