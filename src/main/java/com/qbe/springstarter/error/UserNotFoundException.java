package com.qbe.springstarter.error;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(final Long id) {
        super("User not found with id: " + id);
    }
}
