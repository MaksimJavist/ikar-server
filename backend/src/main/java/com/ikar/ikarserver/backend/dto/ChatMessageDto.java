package com.ikar.ikarserver.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class ChatMessageDto {

    private String senderUuid;

    private String sender;

    private LocalDateTime timeMessage;

    private String message;

}
