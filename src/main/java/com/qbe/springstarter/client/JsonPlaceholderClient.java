package com.qbe.springstarter.client;

import com.qbe.springstarter.dto.UserDto;
import com.qbe.springstarter.error.UserNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Slf4j
public class JsonPlaceholderClient {

    private final RestClient restClient;

    public JsonPlaceholderClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public UserDto getUser(Long id) {
        log.info("id={}", id);
        return restClient
                .get()
                .uri(uriBuilder -> uriBuilder.path("/users/{id}").build(id))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    throw new UserNotFoundException(id);
                })
                .body(UserDto.class);
    }
}
