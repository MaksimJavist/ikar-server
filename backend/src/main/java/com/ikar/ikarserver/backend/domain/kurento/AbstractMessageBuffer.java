package com.ikar.ikarserver.backend.domain.kurento;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ikar.ikarserver.backend.domain.entity.ChatMessage;
import com.ikar.ikarserver.backend.dto.ChatMessageDto;
import com.ikar.ikarserver.backend.service.ChatMessageService;
import com.ikar.ikarserver.backend.util.ChatMessageUtil;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public abstract class AbstractMessageBuffer<T extends ChatMessage> {

    protected final String callIdentifier;
    protected final List<ChatMessageDto> buffer = new CopyOnWriteArrayList<>();

    private final ChatMessageService<T> messageService;

    public void addNewMessageInBuffer(ChatMessageDto message) {
        synchronized (buffer) {
            if (buffer.size() >= 20) {
                List<T> messages = getConvertedMessages();
                messageService.addAllMessages(messages);
                buffer.clear();
            }
            buffer.add(message);
        }
    }

    public JsonArray getAllMessagesForSend() {
        final List<ChatMessageDto> messages = getAllMessages();
        final JsonArray messagesForSending = new JsonArray();

        messages.forEach(message -> {
            final JsonObject messageEl = ChatMessageUtil.convertMessageToJson(message);
            messagesForSending.add(messageEl);
        });

        return messagesForSending;
    }

    private List<ChatMessageDto> getAllMessages() {
        synchronized (buffer) {
            List<ChatMessageDto> roomMessages = messageService
                    .getAllMessagesByUuid(callIdentifier).stream()
                    .map(ChatMessageUtil::convertChatMessageToDto)
                    .collect(Collectors.toList());
            roomMessages.addAll(buffer);
            return roomMessages;
        }
    }

    protected abstract List<T> getConvertedMessages();

}
