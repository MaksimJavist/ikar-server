package com.ikar.ikarserver.backend.handler.message.conference;

import com.google.gson.JsonObject;
import com.ikar.ikarserver.backend.domain.kurento.newconference.NewConference;
import com.ikar.ikarserver.backend.domain.kurento.newconference.NewConferenceUserRegistry;
import com.ikar.ikarserver.backend.exception.websocket.ConferenceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

import static com.ikar.ikarserver.backend.util.Messages.CONFERENCE_NOT_FOUND;

@Component
@RequiredArgsConstructor
public class OnIceCandidateConferenceMessageHandler implements ConferenceMessageHandler {

    private final NewConferenceUserRegistry userRegistry;

    @Override
    public void process(JsonObject message, WebSocketSession session) throws IOException {
        NewConference conference = userRegistry.getConferenceBySession(session)
                .orElseThrow(ConferenceException.supplier(CONFERENCE_NOT_FOUND));
        conference.addIceCandidate(message, session);
    }

    @Override
    public String getProcessedMessage() {
        return "onIceCandidate";
    }
}
