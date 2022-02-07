package com.ikar.ikarserver.backend.exception.app;

import java.text.MessageFormat;
import java.util.function.Supplier;

public class NotFoundException extends AppException {

    public NotFoundException(String message) {
        super(message);
    }

    public static Supplier<NotFoundException> supplier(String message, Object... objects) {
        return () -> new NotFoundException(MessageFormat.format(message, objects));
    }

}
