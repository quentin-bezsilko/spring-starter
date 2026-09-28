package com.qbe.springstarter.error;

import com.qbe.springstarter.constants.ErrorCodesConstants;

public class NotFoundException extends BusinessException {

    public NotFoundException(String resource, Object id) {
        super(ErrorCodesConstants.RESOURCE_NOT_FOUND, "%s with id %s was not found".formatted(resource, id));
    }
}
