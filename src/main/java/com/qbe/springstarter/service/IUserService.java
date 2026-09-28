package com.qbe.springstarter.service;

import com.qbe.springstarter.dto.UserDto;

public interface IUserService {

    UserDto getUser(Long id);
}
