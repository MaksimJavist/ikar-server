package com.ikar.ikarserver.backend.exception.websocket;

public abstract class CallException extends Exception {

    public CallException(String message) {
        super(message);
    }

}