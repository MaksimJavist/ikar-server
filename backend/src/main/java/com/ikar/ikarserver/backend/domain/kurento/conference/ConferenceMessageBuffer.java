package com.ikar.ikarserver.backend.domain.kurento.conference;

import com.ikar.ikarserver.backend.domain.entity.ConferenceChatMessage;
import com.ikar.ikarserver.backend.domain.kurento.AbstractMessageBuffer;
import com.ikar.ikarserver.backend.service.ChatMessageService;
import com.ikar.ikarserver.backend.util.ChatMessageUtil;

import java.util.List;
import java.util.stream.Collectors;

public class ConferenceMessageBuffer extends AbstractMessageBuffer<ConferenceChatMessage> {

    public ConferenceMessageBuffer(String callIdentifier, ChatMessageService<ConferenceChatMessage> messageService) {
        super(callIdentifier, messageService);
    }

    @Override
    protected List<ConferenceChatMessage> getConvertedMessages() {
        return buffer.stream()
                .map(message -> ChatMessageUtil.convertToConferenceMessage(message, callIdentifier))
                .collect(Collectors.toList());
    }

}
