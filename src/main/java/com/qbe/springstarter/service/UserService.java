package com.qbe.springstarter.service;

import com.qbe.springstarter.client.JsonPlaceholderClient;
import com.qbe.springstarter.dto.UserDto;
import org.springframework.stereotype.Service;

@Service
public class UserService implements IUserService {

    private final JsonPlaceholderClient client;

    public UserService(JsonPlaceholderClient client) {
        this.client = client;
    }

    public UserDto getUser(Long id) {
        return client.getUser(id);
    }
}
