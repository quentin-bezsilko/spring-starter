package com.qbe.springstarter.error;

public class EmptyImportFileException extends RuntimeException {

    public EmptyImportFileException(final String message) {
        super(message);
    }
}
