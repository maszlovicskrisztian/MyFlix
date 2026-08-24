package com.maszlovicskrisztian.myflix_core.exception.handler;

import com.maszlovicskrisztian.myflix_core.dtos.response.ApiErrorResponse;
import com.maszlovicskrisztian.myflix_core.exception.MediaProcessingException;
import com.maszlovicskrisztian.myflix_core.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;

@RestControllerAdvice
@Slf4j
public class CustomExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        log.error(ex.getLocalizedMessage(), ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(MediaProcessingException.class)
    public ResponseEntity<ApiErrorResponse> handleMediaProcessing(MediaProcessingException ex) {
        log.error(ex.getLocalizedMessage(), ex);

        HttpStatus status = switch (ex.getReason()) {
            case TIMEOUT -> HttpStatus.GATEWAY_TIMEOUT;
            case IO_ERROR, INTERRUPTED -> HttpStatus.INTERNAL_SERVER_ERROR;
        };

        return ResponseEntity.status(status).body(new ApiErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception ex) {
        log.error(ex.getLocalizedMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiErrorResponse("Unexpected error occurred"));
    }

    @ExceptionHandler(AsyncRequestNotUsableException.class)
    public void handleClientDisconnect(AsyncRequestNotUsableException ex) {
        log.debug("Client closed the stream connection: {}", ex.getMessage());
    }
}
