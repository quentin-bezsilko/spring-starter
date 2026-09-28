package com.qbe.springstarter.error;

import com.qbe.springstarter.constants.ErrorCodesConstants;

public class TechnicalException extends BusinessException {

    public TechnicalException(String message) {
        super(ErrorCodesConstants.TECHNICAL_ERROR, message);
    }
}
