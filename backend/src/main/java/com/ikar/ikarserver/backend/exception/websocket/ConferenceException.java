package com.ikar.ikarserver.backend.exception.websocket;

import java.util.function.Supplier;

public class ConferenceException extends CallException {

    public ConferenceException(String message) {
        super(message);
    }

    public static Supplier<ConferenceException> supplier(String message) {
        return () -> new ConferenceException(message);
    }

}