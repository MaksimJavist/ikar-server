package com.ikar.ikarserver.backend.domain.kurento.newconference;

import com.google.gson.JsonObject;
import com.ikar.ikarserver.backend.domain.CustomUserDetails;
import com.ikar.ikarserver.backend.domain.kurento.conference.ConferenceUserSession;
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
public class NewConference implements Closeable {

    private final String identifier;
    private final ConcurrentHashMap<String, UserSession> viewers = new ConcurrentHashMap<>();

    private final AuthInfoService authInfoService;
    private final KurentoClient kurento;

    private MediaPipeline pipeline;
    private UserSession presenter;

    public UserSession registerViewer(WebSocketSession session) throws IOException {
        UserSession viewer = new UserSession(session);
        viewers.put(session.getId(), viewer);
        ConferenceSender.sendViewerRegisterSuccess(viewer);
        return viewer;
    }

    public void viewerConnectPermission(WebSocketSession session) throws IOException {
        UserSession user = viewers.get(session.getId());
        if (presenter != null) {
            ConferenceSender.sendAcceptViewerConnectPermissionResponse(user);
        } else {
            ConferenceSender.sendRejectViewerConnectPermissionResponse(user);
        }
    }

    public void presenterConnectPermission(WebSocketSession session) throws IOException {
        UserSession user = viewers.get(session.getId());
        if (presenter != null) {
            ConferenceSender.sendRejectPresenterConnectPermissionResponse(user);
        } else {
            ConferenceSender.sendAcceptPresenterConnectPermissionResponse(user);
        }
    }

    public synchronized void presenter(final WebSocketSession session, JsonObject jsonMessage)
            throws IOException {
        if (presenter == null) {
            newPresenter(session, jsonMessage);
        } else {
            rejectPresenter(session);
        }
    }

    public synchronized void viewer(final WebSocketSession session, JsonObject jsonMessage)
            throws IOException {
        UserSession user = viewers.get(session.getId());
        if (user == null) {
            rejectViewer(session, CONFERENCE_USER_NOT_FOUND);
        }
        newViewer(session, jsonMessage);
    }

    private void rejectPresenter(WebSocketSession session) throws IOException {
        UserSession user = viewers.get(session.getId());
        ConferenceSender.sendRejectPresenterResponse(user);
    }

    private void newPresenter(final WebSocketSession session, JsonObject jsonMessage)
            throws IOException {
        final String sessionId = session.getId();
        presenter = viewers.get(sessionId);
        viewers.remove(sessionId);

        pipeline = kurento.createMediaPipeline();
        presenter.setWebRtcEndpoint(new WebRtcEndpoint.Builder(pipeline).build());

        WebRtcEndpoint presenterWebRtc = presenter.getWebRtcEndpoint();

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

        String sdpOffer = jsonMessage.getAsJsonPrimitive("sdpOffer").getAsString();
        String sdpAnswer = presenterWebRtc.processOffer(sdpOffer);

        JsonObject response = new JsonObject();
        response.addProperty("id", "presenterResponse");
        response.addProperty("response", "accepted");
        response.addProperty("sdpAnswer", sdpAnswer);

        synchronized (session) {
            presenter.sendMessage(response);
        }
        presenterWebRtc.gatherCandidates();
        ConferenceSender.sendNewPresenterForAllViewers(viewers.values(), "Иван иванов");
    }

    private void newViewer(final WebSocketSession session, JsonObject jsonMessage)
            throws IOException {
        UserSession viewer = viewers.get(session.getId());

        WebRtcEndpoint nextWebRtc = new WebRtcEndpoint.Builder(pipeline).build();
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

        viewer.setWebRtcEndpoint(nextWebRtc);
        presenter.getWebRtcEndpoint().connect(nextWebRtc);
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

    private void rejectViewer(WebSocketSession session, String message) throws IOException {
        JsonObject response = new JsonObject();
        response.addProperty("id", "viewerResponse");
        response.addProperty("response", "rejected");
        response.addProperty("message", message);
        session.sendMessage(new TextMessage(response.toString()));
    }

    public synchronized void leave(WebSocketSession session) throws IOException {
        String sessionId = session.getId();
        if (presenter != null && presenter.getSession().getId().equals(sessionId)) {
            for (UserSession viewer : viewers.values()) {
                JsonObject response = new JsonObject();
                response.addProperty("id", "presenterLeave");
                response.addProperty("message", "Пользователь " + "Иван Иванов" + " прекратил трансляцию.");
                viewer.sendMessage(response);
                viewer.close();
            }
            presenter.close();
            presenter = null;
        } else if (viewers.containsKey(sessionId)) {
            if (viewers.get(sessionId).getWebRtcEndpoint() != null) {
                viewers.get(sessionId).close();
            }
            viewers.remove(sessionId);
        }
    }

    public synchronized void stop(WebSocketSession session) throws IOException {
        String sessionId = session.getId();
        if (presenter != null && presenter.getSession().getId().equals(sessionId)) {
            for (UserSession viewer : viewers.values()) {
                JsonObject response = new JsonObject();
                response.addProperty("id", "stopCommunication");
                viewer.sendMessage(response);
            }

            log.info("Releasing media pipeline");
            if (pipeline != null) {
                pipeline.release();
            }
            pipeline = null;
            presenter = null;
        } else if (viewers.containsKey(sessionId)) {
            if (viewers.get(sessionId).getWebRtcEndpoint() != null) {
                viewers.get(sessionId).getWebRtcEndpoint().release();
            }
            viewers.remove(sessionId);
        }
    }

    public void addIceCandidate(JsonObject jsonMessage, WebSocketSession session) {
        JsonObject candidate = jsonMessage.get("candidate").getAsJsonObject();

        UserSession user = getUserBySession(session);
        if (user != null) {
            IceCandidate cand =
                    new IceCandidate(candidate.get("candidate").getAsString(), candidate.get("sdpMid")
                            .getAsString(), candidate.get("sdpMLineIndex").getAsInt());
            user.addCandidate(cand);
        }
    }

    private UserSession getUserBySession(WebSocketSession session) {
        if (presenter != null && presenter.getSession() == session) {
            return presenter;
        }
        return viewers.get(session.getId());
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

    @Override
    public void close() throws IOException {
        pipeline.release();
        presenter.close();
        for (UserSession user : viewers.values()) {
            user.close();
        }
    }
}
