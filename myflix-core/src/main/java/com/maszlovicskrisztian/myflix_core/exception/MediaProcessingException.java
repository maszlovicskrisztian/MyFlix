package com.maszlovicskrisztian.myflix_core.exception;

import com.maszlovicskrisztian.myflix_core.dtos.enums.ExceptionReason;
import lombok.Getter;

@Getter
public class MediaProcessingException extends RuntimeException {
    private final ExceptionReason reason;

    public MediaProcessingException(String message, ExceptionReason reason, Throwable cause) {
        super(message, cause);
        this.reason = reason;
    }
}
