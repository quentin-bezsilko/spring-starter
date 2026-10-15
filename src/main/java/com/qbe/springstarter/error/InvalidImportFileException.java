package com.qbe.springstarter.error;

public class InvalidImportFileException extends RuntimeException {

    public InvalidImportFileException(final String message) {
        super(message);
    }
}
