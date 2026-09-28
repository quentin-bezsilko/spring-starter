package com.qbe.springstarter.config;

import com.qbe.springstarter.properties.ClientProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    @Qualifier("jsonPlaceholderRestClient")
    public RestClient jsonPlaceholderRestClient(ClientProperties properties) {
        return RestClient.builder().baseUrl(properties.typicodeBaseUrl()).build();
    }
}
