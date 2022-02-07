package com.ikar.ikarserver.backend.domain.kurento.room;

import com.ikar.ikarserver.backend.domain.entity.RoomChatMessage;
import com.ikar.ikarserver.backend.domain.kurento.AbstractMessageBuffer;
import com.ikar.ikarserver.backend.service.ChatMessageService;
import com.ikar.ikarserver.backend.util.ChatMessageUtil;

import java.util.List;
import java.util.stream.Collectors;

public class RoomMessagesBuffer extends AbstractMessageBuffer<RoomChatMessage> {

    public RoomMessagesBuffer(String callIdentifier, ChatMessageService<RoomChatMessage> messageService) {
        super(callIdentifier, messageService);
    }

    @Override
    protected List<RoomChatMessage> getConvertedMessages() {
        return buffer.stream()
                .map(message -> ChatMessageUtil.convertToRoomMessage(message, callIdentifier))
                .collect(Collectors.toList());
    }
}
