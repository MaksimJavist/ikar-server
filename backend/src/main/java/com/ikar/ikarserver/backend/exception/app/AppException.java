package com.ikar.ikarserver.backend.exception.app;

public abstract class AppException extends RuntimeException {

    protected AppException(String message) {
        super(message);
    }

}
