package com.ikar.ikarserver.exception;

import java.util.function.Supplier;

public abstract class AppException extends RuntimeException {

    protected AppException(String message) {
        super(message);
    }

}
