package com.ikar.ikarserver.backend.service.impl;

import com.ikar.ikarserver.backend.domain.entity.ConferenceChatMessage;
import com.ikar.ikarserver.backend.repository.jpa.ConferenceChatMessageJpaRepository;
import com.ikar.ikarserver.backend.service.AbstractChatMessageService;
import org.springframework.stereotype.Service;

@Service
public class ConferenceChatMessageServiceImpl extends AbstractChatMessageService<ConferenceChatMessage> {

    public ConferenceChatMessageServiceImpl(ConferenceChatMessageJpaRepository repository) {
        super(repository);
    }

}
