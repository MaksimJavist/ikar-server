package com.ikar.ikarserver.backend.service;

import com.ikar.ikarserver.backend.domain.entity.ChatMessage;
import com.ikar.ikarserver.backend.repository.jpa.ChatMessageJpaRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class AbstractChatMessageService<T extends ChatMessage> implements ChatMessageService<T> {

    private final ChatMessageJpaRepository<T> repository;

    @Override
    public void addAllMessages(@NonNull List<T> messages) {
        repository.saveAll(messages);
    }

    @Override
    public List<T> getAllMessagesByUuid(@NonNull String callUuid) {
        return repository.getAllByCallIdentifierOrderByDateTimeMessageAsc(callUuid);
    }
}
