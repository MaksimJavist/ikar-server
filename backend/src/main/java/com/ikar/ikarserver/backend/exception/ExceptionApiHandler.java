package com.ikar.ikarserver.backend.exception;

import com.ikar.ikarserver.backend.domain.ErrorResponse;
import com.ikar.ikarserver.backend.exception.app.AppException;
import com.ikar.ikarserver.backend.exception.app.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@RestControllerAdvice
public class ExceptionApiHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> notFoundException(NotFoundException exception) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .contentType(APPLICATION_JSON)
                .body(ErrorResponse.of(exception.getMessage()));
    }

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ErrorResponse> creationException(AppException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(APPLICATION_JSON)
                .body(ErrorResponse.of(exception.getMessage()));
    }

}
