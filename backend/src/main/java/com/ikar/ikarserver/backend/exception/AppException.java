package com.ikar.ikarserver.backend.exception;

public abstract class AppException extends RuntimeException {

    protected AppException(String message) {
        super(message);
    }

}
