package com.ikar.ikarserver.backend.handler.message.conference;

import com.google.gson.JsonObject;
import com.ikar.ikarserver.backend.domain.kurento.newconference.NewConference;
import com.ikar.ikarserver.backend.domain.kurento.newconference.NewConferenceManager;
import com.ikar.ikarserver.backend.domain.kurento.newconference.NewConferenceUserRegistry;
import com.ikar.ikarserver.backend.domain.kurento.newconference.UserSession;
import com.ikar.ikarserver.backend.exception.websocket.ConferenceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

import static com.ikar.ikarserver.backend.util.Messages.CONFERENCE_NOT_FOUND;

@Component
@RequiredArgsConstructor
public class RegisterViewerConferenceMessageHandler implements ConferenceMessageHandler {

    private final NewConferenceManager conferenceManager;
    private final NewConferenceUserRegistry userRegistry;

    @Override
    public void process(JsonObject message, WebSocketSession session) throws IOException {
        String conferenceIdentifier = message.get("conference").getAsString();
        NewConference conference = conferenceManager.getConference(conferenceIdentifier)
                .orElseThrow(ConferenceException.supplier(CONFERENCE_NOT_FOUND));
        String name = message.get("name").getAsString();

        UserSession registeredUser = conference.registerViewer(session, name);
        userRegistry.register(registeredUser, conference);
    }

    @Override
    public String getProcessedMessage() {
        return "registerViewer";
    }

}
