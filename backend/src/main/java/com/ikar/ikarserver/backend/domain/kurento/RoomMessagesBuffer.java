package com.ikar.ikarserver.backend.domain.kurento;

import com.ikar.ikarserver.backend.domain.entity.ChatMessage;
import com.ikar.ikarserver.backend.domain.entity.RoomChatMessage;
import com.ikar.ikarserver.backend.dto.ChatMessageDto;
import com.ikar.ikarserver.backend.service.RoomChatMessageService;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class RoomMessagesBuffer {

    private final RoomChatMessageService messageService;
    private final String roomUuid;
    private final List<ChatMessageDto> buffer = new CopyOnWriteArrayList<>();

    private static ChatMessageDto convertChatMessagesToDto(ChatMessage message) {
        ChatMessageDto messageDto = new ChatMessageDto();
        messageDto.setSenderUuid(message.getSenderUuid());
        messageDto.setSender(message.getSenderName());
        messageDto.setTimeMessage(message.getDateTimeMessage());
        messageDto.setMessage(message.getText());
        return messageDto;
    }

    public List<ChatMessageDto> getAllRoomMessages() {
        synchronized (buffer) {
            List<ChatMessageDto> roomMessages = messageService
                    .getAllMessagesByUuid(roomUuid)
                    .stream().map(RoomMessagesBuffer::convertChatMessagesToDto)
                    .collect(Collectors.toList());
            roomMessages.addAll(buffer);
            return roomMessages;
        }
    }

    public void addNewMessageInBuffer(ChatMessageDto message) {
        synchronized (buffer) {
            if (buffer.size() >= 20) {
                List<RoomChatMessage> messages = getConvertedMessages();
                messageService.addAllMessages(messages);
                buffer.clear();
            }
            buffer.add(message);
        }
    }

    private List<RoomChatMessage> getConvertedMessages() {
        return buffer.stream()
                .map(message -> {
                    RoomChatMessage chatMessage = new RoomChatMessage();
                    chatMessage.setUuid(
                            UUID.randomUUID().toString()
                    );
                    chatMessage.setSenderUuid(message.getSenderUuid());
                    chatMessage.setRoomIdentifier(roomUuid);
                    chatMessage.setDateTimeMessage(message.getTimeMessage());
                    chatMessage.setSenderName(message.getSender());
                    chatMessage.setText(message.getMessage());
                    return chatMessage;
                })
                .collect(Collectors.toList());
    }

}
