package com.ikar.ikarserver.backend.handler.message.room;

import com.google.gson.JsonObject;
import com.ikar.ikarserver.backend.domain.kurento.room.Room;
import com.ikar.ikarserver.backend.domain.kurento.room.RoomManager;
import com.ikar.ikarserver.backend.domain.kurento.room.RoomUserRegistry;
import com.ikar.ikarserver.backend.domain.kurento.room.RoomUserSession;
import com.ikar.ikarserver.backend.dto.ChatMessageDto;
import com.ikar.ikarserver.backend.exception.websocket.RoomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.time.LocalDateTime;

import static com.ikar.ikarserver.backend.util.Messages.CALL_USER_NOT_EXIST;

@Component
@RequiredArgsConstructor
public class SendChatRoomMessageHandler implements RoomMessageHandler {

    private final RoomManager roomManager;
    private final RoomUserRegistry registry;

    @Override
    public void process(JsonObject message, WebSocketSession session) {
        RoomUserSession messageSender = registry.getBySession(session)
                .orElseThrow(RoomException.supplier(CALL_USER_NOT_EXIST));
        Room room = roomManager.getRoom(messageSender.getRoomUuid());
        String chatMessage = message.get("message").getAsString();
        room.sendNewMessage(
                new ChatMessageDto(
                        messageSender.getUuid(),
                        messageSender.getName(),
                        LocalDateTime.now(),
                        chatMessage
                )
        );
    }

    @Override
    public String getProcessedMessage() {
        return "sendChat";
    }
}
