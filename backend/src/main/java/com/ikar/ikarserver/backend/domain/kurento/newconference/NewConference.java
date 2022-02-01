package com.ikar.ikarserver.backend.domain.kurento.newconference;

import com.google.gson.JsonObject;
import com.ikar.ikarserver.backend.domain.CustomUserDetails;
import com.ikar.ikarserver.backend.dto.ChatMessageDto;
import com.ikar.ikarserver.backend.exception.websocket.ConferenceException;
import com.ikar.ikarserver.backend.service.AuthInfoService;
import com.ikar.ikarserver.backend.service.ConferenceChatMessageService;
import com.ikar.ikarserver.backend.util.ConferenceSender;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.kurento.client.*;
import org.kurento.jsonrpc.JsonUtils;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.Closeable;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.ikar.ikarserver.backend.util.Messages.CONFERENCE_USER_EXIST;
import static com.ikar.ikarserver.backend.util.Messages.CONFERENCE_USER_NOT_FOUND;

@Slf4j
@Getter
@Setter
public class NewConference implements Closeable {

    private final String identifier;
    private final AuthInfoService authInfoService;
    private final ConferenceChatMessageService messageService;
    private final MediaPipeline pipeline;
    private final ConferenceMessageBuffer messageBuffer;

    private final ConcurrentHashMap<String, UserSession> viewers = new ConcurrentHashMap<>();
    private final LocalDateTime creationTime = LocalDateTime.now();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private UserSession presenter;

    public NewConference(String identifier, AuthInfoService authInfoService, ConferenceChatMessageService messageService, MediaPipeline pipeline) {
        this.identifier = identifier;
        this.authInfoService = authInfoService;
        this.messageService = messageService;
        this.pipeline = pipeline;
        this.messageBuffer = new ConferenceMessageBuffer(messageService, identifier);
    }

    public UserSession registerViewer(WebSocketSession session, String username) throws IOException, ConferenceException {
        if (getUserBySession(session) != null) {
            throw new ConferenceException(CONFERENCE_USER_EXIST);
        }
        String uuid = getUserUuid(session);
        UserSession viewer = new UserSession(uuid, username, identifier, session);
        viewers.put(session.getId(), viewer);
        ConferenceSender.sendViewerRegisterSuccess(
                viewer,
                messageBuffer.getAllConferenceMessagesForSend()
        );
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
            return;
        }
        rejectPresenter(session);
    }

    public synchronized void viewer(final WebSocketSession session, JsonObject jsonMessage)
            throws IOException {
        UserSession user = viewers.get(session.getId());
        if (user == null) {
            ConferenceSender.sendRejectViewerResponse(session, CONFERENCE_USER_NOT_FOUND);
            return;
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
        presenter.setWebRtcEndpoint(new WebRtcEndpoint.Builder(pipeline).build());
        WebRtcEndpoint presenterWebRtc = presenter.getWebRtcEndpoint();
        presenterWebRtc.addIceCandidateFoundListener(
                getCandidateEventListener(session)
        );

        String sdpOffer = jsonMessage.getAsJsonPrimitive("sdpOffer").getAsString();
        String sdpAnswer = presenterWebRtc.processOffer(sdpOffer);

        synchronized (session) {
            ConferenceSender.sendPresenterResponseSdpAnswer(presenter, sdpAnswer);
        }
        presenterWebRtc.gatherCandidates();

        synchronized (presenter) {
            ConferenceSender.sendNewPresenterForAllViewers(viewers.values(), presenter.getUsername());
        }
    }

    private void newViewer(final WebSocketSession session, JsonObject jsonMessage)
            throws IOException {
        UserSession viewer = viewers.get(session.getId());

        WebRtcEndpoint nextWebRtc = new WebRtcEndpoint.Builder(pipeline).build();
        nextWebRtc.addIceCandidateFoundListener(
                getCandidateEventListener(session)
        );

        viewer.setWebRtcEndpoint(nextWebRtc);
        presenter.getWebRtcEndpoint().connect(nextWebRtc);
        String sdpOffer = jsonMessage.getAsJsonPrimitive("sdpOffer").getAsString();
        String sdpAnswer = nextWebRtc.processOffer(sdpOffer);

        ConferenceSender.sendViewerResponseSdpAnswer(viewer, presenter.getUsername(), sdpAnswer);
        nextWebRtc.gatherCandidates();
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

    public void sendNewMessage(ChatMessageDto chatMessage) throws IOException {
        executor.submit(
                () -> messageBuffer.addNewMessageInBuffer(chatMessage)
        );
        sendAllUsersNewChatMessage(chatMessage);
    }

    public void leave(WebSocketSession session) throws IOException {
        String sessionId = session.getId();
        if (isPresenter(sessionId)) {
            stop(session);
        } else if (viewers.containsKey(sessionId)) {
            closePeerConnection(sessionId);
        }
        viewers.remove(sessionId);
    }

    public synchronized void stop(WebSocketSession session) throws IOException {
        String sessionId = session.getId();
        if (isPresenter(sessionId)) {
            ConferenceSender.sendPresenterStopForAllViewers(presenter.getUsername(), viewers.values());
            for (UserSession viewer : viewers.values()) {
                viewer.close();
            }
            viewers.put(session.getId(), presenter);
            presenter.close();
            presenter = null;
        } else if (viewers.containsKey(sessionId)) {
            closePeerConnection(sessionId);
        }
    }

    private void sendAllUsersNewChatMessage(ChatMessageDto chatMessage) throws IOException {
        final List<UserSession> allUsersForSend = new ArrayList<>(viewers.values());
        if (presenter != null) {
            allUsersForSend.add(presenter);
        }
        ConferenceSender.sendAllUsersNewChatMessage(chatMessage, allUsersForSend);
    }

    private UserSession getUserBySession(WebSocketSession session) {
        if (presenter != null && presenter.getSession() == session) {
            return presenter;
        }
        return viewers.get(session.getId());
    }

    private boolean isPresenter(String sessionId) {
        return presenter != null && presenter.getSession().getId().equals(sessionId);
    }

    private void closePeerConnection(String sessionId) {
        final UserSession user = viewers.get(sessionId);
        if (user != null && user.getWebRtcEndpoint() != null) {
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

    private EventListener<IceCandidateFoundEvent> getCandidateEventListener(WebSocketSession session) {
        return event -> {
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
        };
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
