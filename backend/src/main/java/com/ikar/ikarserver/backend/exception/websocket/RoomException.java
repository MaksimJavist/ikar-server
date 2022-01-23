package com.ikar.ikarserver.backend.exception.websocket;

import java.util.function.Supplier;

public class RoomException extends CallException {

    public RoomException(String message) {
        super(message);
    }

    public static Supplier<RoomException> supplier(String message) {
        return () -> new RoomException(message);
    }

}
