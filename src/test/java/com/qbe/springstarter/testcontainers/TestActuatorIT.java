package com.qbe.springstarter.testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;

class TestActuatorIT extends TestAbstractIntegration {

    @LocalServerPort
    private int port;

    private final RestClient restClient = RestClient.create();

    @Test
    void healthEndpointShouldBeAvailable() {
        var response = restClient
                .get()
                .uri("http://localhost:" + port + "/actuator/health")
                .retrieve()
                .toEntity(String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(response.getBody()).contains("UP");
    }
}
