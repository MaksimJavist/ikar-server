package com.ikar.ikarserver.backend.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ChatMessage {

    private String uuid;

    private String sender;

    private String message;

    private LocalDateTime timeMessage;

}
