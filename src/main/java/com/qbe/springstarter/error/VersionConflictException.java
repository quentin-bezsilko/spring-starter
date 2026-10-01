package com.qbe.springstarter.error;

public class VersionConflictException extends RuntimeException {

    public VersionConflictException(Long id, Long dtoVersion, Long entityVersion) {
        super("Version conflict for entity with id: " + id + ". DTO version: " + dtoVersion + ", Entity version: "
                + entityVersion);
    }
}
