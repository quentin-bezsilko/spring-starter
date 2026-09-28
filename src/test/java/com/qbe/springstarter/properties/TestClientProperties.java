package com.qbe.springstarter.properties;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TestClientProperties {

    @Test
    void shouldCreateRecord() {
        ClientProperties properties = new ClientProperties("https://jsonplaceholder.typicode.com");
        assertThat(properties.typicodeBaseUrl()).isEqualTo("https://jsonplaceholder.typicode.com");
    }
}
