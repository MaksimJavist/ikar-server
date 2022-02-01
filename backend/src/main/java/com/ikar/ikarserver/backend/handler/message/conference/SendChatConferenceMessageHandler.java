package com.ikar.ikarserver.backend.handler.message.conference;

import com.google.gson.JsonObject;
import com.ikar.ikarserver.backend.domain.kurento.conference.Conference;
import com.ikar.ikarserver.backend.domain.kurento.conference.ConferenceManager;
import com.ikar.ikarserver.backend.domain.kurento.conference.ConferenceUserRegistry;
import com.ikar.ikarserver.backend.domain.kurento.conference.ConferenceUserSession;
import com.ikar.ikarserver.backend.dto.ChatMessageDto;
import com.ikar.ikarserver.backend.exception.websocket.ConferenceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.time.LocalDateTime;

import static com.ikar.ikarserver.backend.util.Messages.CONFERENCE_NOT_FOUND;
import static com.ikar.ikarserver.backend.util.Messages.CONFERENCE_USER_NOT_EXIST;

@Component
@RequiredArgsConstructor
public class SendChatConferenceMessageHandler implements ConferenceMessageHandler {

    private final ConferenceManager conferenceManager;
    private final ConferenceUserRegistry userRegistry;

    @Override
    public void process(JsonObject message, WebSocketSession session) throws IOException {
        ConferenceUserSession user = userRegistry.getBySession(session)
                .orElseThrow(ConferenceException.supplier(CONFERENCE_USER_NOT_EXIST));;
        Conference conference = conferenceManager.getConference(user.getConferenceIdentifier())
                .orElseThrow(ConferenceException.supplier(CONFERENCE_NOT_FOUND));
        String chatMessage = message.get("message").getAsString();
        conference.sendNewMessage(
                new ChatMessageDto(
                        user.getUuid(),
                        user.getUsername(),
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
