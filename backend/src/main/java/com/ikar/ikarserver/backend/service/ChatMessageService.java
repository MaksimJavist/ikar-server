package com.ikar.ikarserver.backend.service;

import com.ikar.ikarserver.backend.domain.entity.ChatMessage;
import lombok.NonNull;

import java.util.List;

public interface ChatMessageService<T extends ChatMessage> {

    void addAllMessages(@NonNull List<T> messages);

    List<T> getAllMessagesByUuid(@NonNull String callUuid);

    void deleteAllMessagesByCallIdentifier(String identifier);

}
