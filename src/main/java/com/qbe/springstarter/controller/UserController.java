package com.qbe.springstarter.controller;

import com.qbe.springstarter.dto.UserDto;
import com.qbe.springstarter.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequiredArgsConstructor
public class UserController implements IUserController {

    private final UserService service;

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUser(@PathVariable Long id) {
        log.info("Finding UserEntity by id={}", id);
        return ResponseEntity.status(HttpStatus.OK).body(service.getUser(id));
    }
}
