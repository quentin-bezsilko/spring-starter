package com.qbe.springstarter.testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.qbe.springstarter.service.ISampleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@WithMockUser(username = "test-user")
class TestSampleFavoriteControllerIT extends TestAbstractIntegration {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext applicationContext;

    @Autowired
    private ISampleService sampleService;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext).build();
    }

    @Test
    void shouldAddFavorite() throws Exception {

        var sample = sampleService.create(buildSampleDto("FAVORITE-SAMPLE-001"));

        mockMvc.perform(post("/api/v1/samples/{id}/favorite", sample.id())).andExpect(status().isCreated());
    }

    @Test
    void shouldRemoveFavorite() throws Exception {

        var sample = sampleService.create(buildSampleDto("FAVORITE-SAMPLE-002"));

        mockMvc.perform(post("/api/v1/samples/{id}/favorite", sample.id())).andExpect(status().isCreated());

        mockMvc.perform(delete("/api/v1/samples/{id}/favorite", sample.id())).andExpect(status().isNoContent());
    }

    @Test
    void shouldReturnNotFoundWhenAddingFavoriteToUnknownSample() throws Exception {

        mockMvc.perform(post("/api/v1/samples/{id}/favorite", 999999L)).andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnNotFoundWhenRemovingFavoriteFromUnknownSample() throws Exception {

        mockMvc.perform(delete("/api/v1/samples/{id}/favorite", 999999L)).andExpect(status().isNotFound());
    }

    @Test
    void shouldAddFavoriteOnlyOnce() throws Exception {
        var sample = sampleService.create(buildSampleDto("FAVORITE-SAMPLE-003"));

        mockMvc.perform(post("/api/v1/samples/{id}/favorite", sample.id())).andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/samples/{id}/favorite", sample.id())).andExpect(status().isCreated());
    }

    @Test
    void shouldMarkSampleAsFavorite() throws Exception {
        var sample = sampleService.create(buildSampleDto("FAVORITE-SAMPLE-004"));

        mockMvc.perform(post("/api/v1/samples/{id}/favorite", sample.id())).andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/samples/{id}", sample.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.favorite").value(true));
    }
}
