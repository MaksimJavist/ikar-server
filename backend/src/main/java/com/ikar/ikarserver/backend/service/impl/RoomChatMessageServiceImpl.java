package com.ikar.ikarserver.backend.service.impl;

import com.ikar.ikarserver.backend.domain.entity.RoomChatMessage;
import com.ikar.ikarserver.backend.repository.jpa.RoomChatMessageJpaRepository;
import com.ikar.ikarserver.backend.service.RoomChatMessageService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomChatMessageServiceImpl implements RoomChatMessageService {

    private final RoomChatMessageJpaRepository repository;

    @Override
    public void addAllMessages(@NonNull List<RoomChatMessage> messages) {
        repository.saveAll(messages);
    }

    @Override
    public List<RoomChatMessage> getAllMessagesByUuid(@NonNull String roomUuid) {
        return repository.getAllByCallIdentifierOrderByDateTimeMessageDesc(roomUuid);
    }

}
