package com.ikar.ikarserver.backend.domain.kurento.newconference;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ikar.ikarserver.backend.domain.entity.ChatMessage;
import com.ikar.ikarserver.backend.domain.entity.ConferenceChatMessage;
import com.ikar.ikarserver.backend.dto.ChatMessageDto;
import com.ikar.ikarserver.backend.service.ConferenceChatMessageService;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ConferenceMessageBuffer {

    private final ConferenceChatMessageService messageService;
    private final String conferenceIdentifier;
    private final List<ChatMessageDto> buffer = new CopyOnWriteArrayList<>();

    private static ChatMessageDto convertChatMessagesToDto(ChatMessage message) {
        ChatMessageDto messageDto = new ChatMessageDto();
        messageDto.setSenderUuid(message.getSenderUuid());
        messageDto.setSender(message.getSenderName());
        messageDto.setTimeMessage(message.getDateTimeMessage());
        messageDto.setMessage(message.getText());
        return messageDto;
    }

    public JsonArray getAllConferenceMessagesForSend() {
        final List<ChatMessageDto> messages = getAllConferenceMessages();
        final JsonArray messagesForSending = new JsonArray();

        messages.forEach(message -> {
            final JsonObject messageEl = convertChatMessageToJson(message);
            messagesForSending.add(messageEl);
        });

        return messagesForSending;
    }

    public void addNewMessageInBuffer(ChatMessageDto message) {
        synchronized (buffer) {
            if (buffer.size() >= 20) {
                List<ConferenceChatMessage> messages = getConvertedMessages();
                messageService.addAllMessages(messages);
                buffer.clear();
            }
            buffer.add(message);
        }
    }

    public static JsonObject convertChatMessageToJson(ChatMessageDto message) {
        final JsonObject messageJson = new JsonObject();
        messageJson.addProperty("senderUuid", message.getSenderUuid());
        messageJson.addProperty("senderName", message.getSender());
        messageJson.addProperty("time", message.getTimeMessage().toString());
        messageJson.addProperty("text", message.getMessage());

        return messageJson;
    }

    private List<ChatMessageDto> getAllConferenceMessages() {
        synchronized (buffer) {
            List<ChatMessageDto> roomMessages = messageService
                    .getAllMessagesByUuid(conferenceIdentifier).stream()
                    .map(ConferenceMessageBuffer::convertChatMessagesToDto)
                    .collect(Collectors.toList());
            roomMessages.addAll(buffer);
            return roomMessages;
        }
    }

    private List<ConferenceChatMessage> getConvertedMessages() {
        return buffer.stream()
                .map(message -> {
                    ConferenceChatMessage chatMessage = new ConferenceChatMessage();
                    chatMessage.setUuid(
                            UUID.randomUUID().toString()
                    );
                    chatMessage.setSenderUuid(message.getSenderUuid());
                    chatMessage.setCallIdentifier(conferenceIdentifier);
                    chatMessage.setDateTimeMessage(message.getTimeMessage());
                    chatMessage.setSenderName(message.getSender());
                    chatMessage.setText(message.getMessage());
                    return chatMessage;
                })
                .collect(Collectors.toList());
    }

}
