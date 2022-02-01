package com.ikar.ikarserver.backend.handler.message.conference;

import com.google.gson.JsonObject;
import com.ikar.ikarserver.backend.domain.kurento.conference.Conference;
import com.ikar.ikarserver.backend.domain.kurento.conference.ConferenceManager;
import com.ikar.ikarserver.backend.domain.kurento.conference.ConferenceUserRegistry;
import com.ikar.ikarserver.backend.domain.kurento.conference.ConferenceUserSession;
import com.ikar.ikarserver.backend.exception.websocket.ConferenceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

import static com.ikar.ikarserver.backend.util.Messages.CONFERENCE_NOT_FOUND;

@Component
@RequiredArgsConstructor
public class RegisterViewerConferenceMessageHandler implements ConferenceMessageHandler {

    private final ConferenceManager conferenceManager;
    private final ConferenceUserRegistry userRegistry;

    @Override
    public void process(JsonObject message, WebSocketSession session) throws IOException {
        String conferenceIdentifier = message.get("conference").getAsString();
        Conference conference = conferenceManager.getConference(conferenceIdentifier)
                .orElseThrow(ConferenceException.supplier(CONFERENCE_NOT_FOUND));
        String name = message.get("name").getAsString();

        ConferenceUserSession registeredUser = conference.registerViewer(session, name);
        userRegistry.register(registeredUser, conference);
    }

    @Override
    public String getProcessedMessage() {
        return "registerViewer";
    }

}
