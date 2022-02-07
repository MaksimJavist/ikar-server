package com.ikar.ikarserver.backend.service.impl;

import com.ikar.ikarserver.backend.domain.entity.RoomChatMessage;
import com.ikar.ikarserver.backend.repository.jpa.RoomChatMessageJpaRepository;
import com.ikar.ikarserver.backend.service.AbstractChatMessageService;
import org.springframework.stereotype.Service;

@Service
public class RoomChatMessageServiceImpl extends AbstractChatMessageService<RoomChatMessage> {

    public RoomChatMessageServiceImpl(RoomChatMessageJpaRepository repository) {
        super(repository);
    }

}
