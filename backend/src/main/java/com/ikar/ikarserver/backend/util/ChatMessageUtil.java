package com.ikar.ikarserver.backend.util;

import com.google.gson.JsonObject;
import com.ikar.ikarserver.backend.domain.entity.ChatMessage;
import com.ikar.ikarserver.backend.domain.entity.ConferenceChatMessage;
import com.ikar.ikarserver.backend.domain.entity.RoomChatMessage;
import com.ikar.ikarserver.backend.dto.ChatMessageDto;
import lombok.experimental.UtilityClass;

import java.util.UUID;

@UtilityClass
public final class ChatMessageUtil {

    public static ChatMessageDto convertChatMessageToDto(ChatMessage message) {
        ChatMessageDto messageDto = new ChatMessageDto();
        messageDto.setSenderUuid(message.getSenderUuid());
        messageDto.setSender(message.getSenderName());
        messageDto.setTimeMessage(message.getDateTimeMessage());
        messageDto.setMessage(message.getText());
        return messageDto;
    }

    public static JsonObject convertMessageToJson(ChatMessageDto message) {
        final JsonObject messageJson = new JsonObject();
        messageJson.addProperty("senderUuid", message.getSenderUuid());
        messageJson.addProperty("senderName", message.getSender());
        messageJson.addProperty("time", message.getTimeMessage().toString());
        messageJson.addProperty("text", message.getMessage());

        return messageJson;
    }

    public static ConferenceChatMessage convertToConferenceMessage(ChatMessageDto message, String identifier) {
        ConferenceChatMessage chatMessage = new ConferenceChatMessage();
        chatMessage.setUuid(
                UUID.randomUUID().toString()
        );
        chatMessage.setSenderUuid(message.getSenderUuid());
        chatMessage.setCallIdentifier(identifier);
        chatMessage.setDateTimeMessage(message.getTimeMessage());
        chatMessage.setSenderName(message.getSender());
        chatMessage.setText(message.getMessage());
        return chatMessage;
    }

    public static RoomChatMessage convertToRoomMessage(ChatMessageDto message, String identifier) {
        RoomChatMessage chatMessage = new RoomChatMessage();
        chatMessage.setUuid(
                UUID.randomUUID().toString()
        );
        chatMessage.setSenderUuid(message.getSenderUuid());
        chatMessage.setCallIdentifier(identifier);
        chatMessage.setDateTimeMessage(message.getTimeMessage());
        chatMessage.setSenderName(message.getSender());
        chatMessage.setText(message.getMessage());
        return chatMessage;
    }

}
