package com.ikar.ikarserver.backend.handler.message;

import com.google.gson.JsonObject;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

public interface MessageHandler {

    void process(JsonObject message, WebSocketSession session) throws IOException;

    String getProcessedMessage();
}
