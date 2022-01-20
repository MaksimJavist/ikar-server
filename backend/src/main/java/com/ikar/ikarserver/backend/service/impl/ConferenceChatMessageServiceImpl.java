package com.ikar.ikarserver.backend.service.impl;

import com.ikar.ikarserver.backend.domain.entity.ConferenceChatMessage;
import com.ikar.ikarserver.backend.repository.jpa.ConferenceChatMessageJpaRepository;
import com.ikar.ikarserver.backend.service.ConferenceChatMessageService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConferenceChatMessageServiceImpl implements ConferenceChatMessageService {

    private final ConferenceChatMessageJpaRepository repository;

    @Override
    public void addAllMessages(@NonNull List<ConferenceChatMessage> messages) {
        repository.saveAll(messages);
    }

    @Override
    public List<ConferenceChatMessage> getAllMessagesByUuid(@NonNull String conferenceUuid) {
        return repository.getAllByCallIdentifierOrderByDateTimeMessageAsc(conferenceUuid);
    }

}
