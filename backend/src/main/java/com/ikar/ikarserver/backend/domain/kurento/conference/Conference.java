package com.ikar.ikarserver.backend.domain.kurento.conference;

import com.google.gson.JsonObject;
import com.ikar.ikarserver.backend.domain.CustomUserDetails;
import com.ikar.ikarserver.backend.exception.websocket.ConferenceException;
import com.ikar.ikarserver.backend.service.AuthInfoService;
import com.ikar.ikarserver.backend.util.ConferenceSender;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.kurento.client.*;
import org.kurento.jsonrpc.JsonUtils;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.Closeable;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.ikar.ikarserver.backend.util.Messages.*;

@Slf4j
@Getter
@Setter
@RequiredArgsConstructor
public class Conference implements Closeable {

    private final AuthInfoService authInfoService;

    private final String identifier;
    private final ConcurrentHashMap<String, ConferenceUserSession> viewers = new ConcurrentHashMap<>();
    private final MediaPipeline pipeline;
    private ConferenceUserSession presenter;

    public synchronized void presenter(final WebSocketSession session, JsonObject jsonMessage)
            throws IOException {
        if (presenter == null) {
            newPresenter(session, jsonMessage);
        } else {
            rejectPresenter(session);
        }
    }

    public ConferenceUserSession registerViewer(WebSocketSession session, String name) throws IOException, ConferenceException {
        if (viewers.containsKey(session.getId())) {
            throw new ConferenceException(CONFERENCE_VIEWED_EXCEPTION);
        }

        String uuid = getUserUuid(session);
        ConferenceUserSession viewer = new ConferenceUserSession(uuid, name, identifier, session);
        viewers.put(session.getId(), viewer);

        ConferenceSender.sendViewerRegisterSuccess(viewer);
        return viewer;
    }

    public synchronized void viewer(final WebSocketSession session, JsonObject jsonMessage)
            throws IOException {
        ConferenceUserSession user = viewers.get(session.getId());
        if (user == null) {
            rejectViewer(session, CONFERENCE_USER_NOT_FOUND);
        }
        if (presenter == null) {
            rejectViewer(session, CONFERENCE_NOT_ACTIVE_PRESENTER);
        } else {
            newViewer(session, jsonMessage);
        }
    }

    public void onIceCandidate(WebSocketSession session, JsonObject candidate) {
        ConferenceUserSession user = getUserBySession(session);

        if (user != null && user.getWebRtcEndpoint() != null) {
            IceCandidate cand =
                    new IceCandidate(candidate.get("candidate").getAsString(), candidate.get("sdpMid")
                            .getAsString(), candidate.get("sdpMLineIndex").getAsInt());
            user.addCandidate(cand);
        }
    }

    public ConferenceUserSession getUserBySession(WebSocketSession session) {
        if (presenter != null && presenter.getSession() == session) {
            return presenter;
        }
        return viewers.get(session.getId());
    }

    public synchronized void stopCommunication(WebSocketSession session) throws IOException, ConferenceException {
        final String sessionId = session.getId();
        if (presenter != null && presenter.getSession().getId().equals(sessionId)) {
            for (ConferenceUserSession viewer : viewers.values()) {
                JsonObject response = new JsonObject();
                response.addProperty("id", "stopCommunication");
                viewer.sendMessage(response);
                viewer.getWebRtcEndpoint().release();
            }

            presenter.close();
            viewers.put(presenter.getSession().getId(), presenter);
            presenter = null;
        } else {
            throw new ConferenceException(CONFERENCE_DOES_NOT_PRESENTER);
        }
    }

    public synchronized void leave(WebSocketSession session) throws IOException {
        String sessionId = session.getId();
        if (presenter != null && presenter.getSession().getId().equals(sessionId)) {
            for (ConferenceUserSession viewer : viewers.values()) {
                JsonObject response = new JsonObject();
                response.addProperty("id", "presenterLeave");
                viewer.sendMessage(response);
                viewer.getWebRtcEndpoint().release();
            }
            presenter.close();
            presenter = null;
        } else if (viewers.containsKey(sessionId)) {
            if (viewers.get(sessionId).getWebRtcEndpoint() != null) {
                viewers.get(sessionId).getWebRtcEndpoint().release();
            }
            viewers.remove(sessionId);
        }
    }

    private void newPresenter(WebSocketSession session, JsonObject message) throws IOException {
        presenter = viewers.get(session.getId());
        viewers.remove(session.getId());
        WebRtcEndpoint presenterWebRtc = new WebRtcEndpoint.Builder(pipeline).build();
        presenter.setWebRtcEndpoint(presenterWebRtc);

        presenterWebRtc.addIceCandidateFoundListener(new EventListener<IceCandidateFoundEvent>() {

            @Override
            public void onEvent(IceCandidateFoundEvent event) {
                JsonObject response = new JsonObject();
                response.addProperty("id", "iceCandidate");
                response.add("candidate", JsonUtils.toJsonObject(event.getCandidate()));
                try {
                    synchronized (session) {
                        session.sendMessage(new TextMessage(response.toString()));
                    }
                } catch (IOException e) {
                    log.debug(e.getMessage());
                }
            }

        });

        String sdpOffer = message.getAsJsonPrimitive("sdpOffer").getAsString();
        String sdpAnswer = presenterWebRtc.processOffer(sdpOffer);

        JsonObject response = new JsonObject();
        response.addProperty("id", "presenterResponse");
        response.addProperty("response", "accepted");
        response.addProperty("sdpAnswer", sdpAnswer);

        synchronized (session) {
            presenter.sendMessage(response);
        }
        presenterWebRtc.gatherCandidates();
        sendNewPresenterForAllRegisteredViewers(presenter.getName());
    }

    private void newViewer(WebSocketSession session, JsonObject jsonMessage) throws IOException {
        ConferenceUserSession viewer = viewers.get(session.getId());
        WebRtcEndpoint nextWebRtc = new WebRtcEndpoint.Builder(pipeline).build();
        viewer.setWebRtcEndpoint(nextWebRtc);

        nextWebRtc.addIceCandidateFoundListener(new EventListener<IceCandidateFoundEvent>() {

            @Override
            public void onEvent(IceCandidateFoundEvent event) {
                JsonObject response = new JsonObject();
                response.addProperty("id", "iceCandidate");
                response.add("candidate", JsonUtils.toJsonObject(event.getCandidate()));
                try {
                    synchronized (session) {
                        session.sendMessage(new TextMessage(response.toString()));
                    }
                } catch (IOException e) {
                    log.debug(e.getMessage());
                }
            }
        });
//        presenter.getWebRtcEndpoint().connect(nextWebRtc);
        String sdpOffer = jsonMessage.getAsJsonPrimitive("sdpOffer").getAsString();
        String sdpAnswer = nextWebRtc.processOffer(sdpOffer);

        JsonObject response = new JsonObject();
        response.addProperty("id", "viewerResponse");
        response.addProperty("response", "accepted");
        response.addProperty("sdpAnswer", sdpAnswer);

        synchronized (session) {
            viewer.sendMessage(response);
        }
        nextWebRtc.gatherCandidates();
    }

    private void rejectPresenter(WebSocketSession session) throws IOException {
        JsonObject response = new JsonObject();
        response.addProperty("id", "presenterResponse");
        response.addProperty("response", "rejected");
        response.addProperty("message", CONFERENCE_PRESENTER_BUSY);
        session.sendMessage(new TextMessage(response.toString()));
    }

    private void rejectViewer(WebSocketSession session, String message) throws IOException {
        JsonObject response = new JsonObject();
        response.addProperty("id", "viewerResponse");
        response.addProperty("response", "rejected");
        response.addProperty("message", message);
        session.sendMessage(new TextMessage(response.toString()));
    }

    private void sendNewPresenterForAllRegisteredViewers(String presenterName) throws IOException {
        JsonObject message = new JsonObject();
        message.addProperty("id", "newPresenter");
        message.addProperty("message", "Пользователь " + presenterName + " начал трансляцию");
        String jsonString = message.toString();
        for (ConferenceUserSession viewer : viewers.values()) {
            viewer.getSession().sendMessage(new TextMessage(jsonString));
        }
    }

    @Override
    public void close() throws IOException {
        pipeline.release();
        presenter.close();
        for (ConferenceUserSession user : viewers.values()) {
            user.close();
        }
    }

    private String getUserUuid(WebSocketSession session) {
        String uuid;
        Optional<CustomUserDetails> optUser = authInfoService.getWebsocketUser(session);
        if (optUser.isPresent()) {
            uuid = optUser.get().getUuid();
        } else {
            uuid = UUID.randomUUID().toString();
        }
        return uuid;
    }

}
