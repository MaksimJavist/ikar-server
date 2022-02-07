package com.ikar.ikarserver.backend.exception.app;

import java.text.MessageFormat;
import java.util.function.Supplier;

public class UpdateException extends AppException {

    public UpdateException(String message) {
        super(message);
    }

    public static Supplier<CreationException> supplier(String message, Object... objects) {
        return () -> new CreationException(MessageFormat.format(message, objects));
    }

}
