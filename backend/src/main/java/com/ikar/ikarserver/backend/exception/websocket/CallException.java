package com.ikar.ikarserver.backend.exception.websocket;

public abstract class CallException extends RuntimeException {

    public CallException(String message) {
        super(message);
    }

}