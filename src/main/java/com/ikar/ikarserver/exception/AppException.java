package com.ikar.ikarserver.exception;

import java.util.function.Supplier;

public abstract class AppException extends RuntimeException {

    protected AppException(String message) {
        super(message);
    }

    public abstract Supplier<? extends AppException> supplier(String message, Object... objects);

}
