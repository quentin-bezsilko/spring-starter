package com.qbe.springstarter.testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@WithMockUser(username = "test-user")
class TestSampleStatisticsControllerIT extends TestAbstractIntegration {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext applicationContext;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext).build();
    }

    @Test
    void shouldReturnStatistics() throws Exception {

        mockMvc.perform(get("/api/v1/samples/statistics"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.overview").exists())
                .andExpect(jsonPath("$.statusDistribution").isArray())
                .andExpect(jsonPath("$.categoryDistribution").isArray())
                .andExpect(jsonPath("$.favoriteDistribution").exists())
                .andExpect(jsonPath("$.stockDistribution").exists())
                .andExpect(jsonPath("$.trashDistribution").exists())
                .andExpect(jsonPath("$.monthlyCreations").isArray())
                .andExpect(jsonPath("$.priceStatistics").exists())
                .andExpect(jsonPath("$.quantityStatistics").exists());
    }

    @Test
    void shouldReturnOverviewStatistics() throws Exception {

        mockMvc.perform(get("/api/v1/samples/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overview.totalSamples").isNumber())
                .andExpect(jsonPath("$.overview.activeSamples").isNumber())
                .andExpect(jsonPath("$.overview.inactiveSamples").isNumber())
                .andExpect(jsonPath("$.overview.favoriteSamples").isNumber())
                .andExpect(jsonPath("$.overview.trashedSamples").isNumber());
    }

    @Test
    void shouldReturnPriceStatistics() throws Exception {

        mockMvc.perform(get("/api/v1/samples/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.priceStatistics.min").exists())
                .andExpect(jsonPath("$.priceStatistics.max").exists())
                .andExpect(jsonPath("$.priceStatistics.average").exists());
    }

    @Test
    void shouldReturnQuantityStatistics() throws Exception {

        mockMvc.perform(get("/api/v1/samples/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantityStatistics.min").exists())
                .andExpect(jsonPath("$.quantityStatistics.max").exists())
                .andExpect(jsonPath("$.quantityStatistics.average").exists());
    }

    @Test
    void shouldReturnStatusDistribution() throws Exception {

        mockMvc.perform(get("/api/v1/samples/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusDistribution").isArray());
    }

    @Test
    void shouldReturnCategoryDistribution() throws Exception {

        mockMvc.perform(get("/api/v1/samples/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryDistribution").isArray());
    }

    @Test
    void shouldReturnMonthlyCreations() throws Exception {

        mockMvc.perform(get("/api/v1/samples/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.monthlyCreations").isArray());
    }

    @Test
    void shouldReturnStatisticsAfterImport() throws Exception {
        ClassPathResource resource = new ClassPathResource("data/sample-import-template.xlsx");

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "sample-import-template.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                resource.getInputStream());

        mockMvc.perform(multipart("/api/v1/samples/import").file(file)).andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/samples/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overview.totalSamples").value(50))
                .andExpect(jsonPath("$.favoriteDistribution.favorites").value(0))
                .andExpect(jsonPath("$.trashDistribution.trashed").value(0));
    }
}
