package com.ikar.ikarserver.backend.domain.kurento.conference;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ikar.ikarserver.backend.domain.CustomUserDetails;
import com.ikar.ikarserver.backend.domain.entity.ConferenceChatMessage;
import com.ikar.ikarserver.backend.dto.ChatMessageDto;
import com.ikar.ikarserver.backend.exception.websocket.ConferenceException;
import com.ikar.ikarserver.backend.exception.websocket.RoomException;
import com.ikar.ikarserver.backend.service.AuthInfoService;
import com.ikar.ikarserver.backend.service.ChatMessageService;
import com.ikar.ikarserver.backend.util.ConferenceSender;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.kurento.client.IceCandidate;
import org.kurento.client.MediaPipeline;
import org.kurento.client.WebRtcEndpoint;
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
import static com.ikar.ikarserver.backend.util.Messages.USER_ARE_NOT_PRESENTER;

@Slf4j
@Getter
@Setter
public class Conference implements Closeable {

    private final String identifier;
    private final AuthInfoService authInfoService;
    private final ChatMessageService<ConferenceChatMessage> messageService;
    private final MediaPipeline pipeline;
    private final ConferenceMessageBuffer messageBuffer;

    private ConferenceUserSession presenter;

    private final ConcurrentHashMap<String, ConferenceUserSession> viewers = new ConcurrentHashMap<>();
    private final LocalDateTime creationTime = LocalDateTime.now();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    @Getter
    private final LocalDateTime createdTime = LocalDateTime.now();

    public Conference(String identifier, AuthInfoService authInfoService, ChatMessageService<ConferenceChatMessage> messageService, MediaPipeline pipeline) {
        this.identifier = identifier;
        this.authInfoService = authInfoService;
        this.messageService = messageService;
        this.pipeline = pipeline;
        this.messageBuffer = new ConferenceMessageBuffer(identifier, messageService);
    }

    public ConferenceUserSession registerViewer(WebSocketSession session, String username) throws IOException, ConferenceException {
        if (getUserBySession(session) != null) {
            throw new ConferenceException(CONFERENCE_USER_EXIST);
        }
        String uuid = getUserUuid(session);
        ConferenceUserSession viewer = new ConferenceUserSession(uuid, username, identifier, session);
        ConferenceSender.sendNewUserJoinForAll(viewers.values(), viewer);
        viewers.put(session.getId(), viewer);
        ConferenceSender.sendViewerRegisterSuccess(
                viewer,
                getConferenceUsersNames(),
                messageBuffer.getAllMessagesForSend()
        );
        return viewer;
    }

    public void viewerConnectPermission(WebSocketSession session) throws IOException {
        ConferenceUserSession user = viewers.get(session.getId());
        if (presenter != null) {
            ConferenceSender.sendAcceptViewerConnectPermissionResponse(user, presenter.getUsername());
        } else {
            ConferenceSender.sendRejectViewerConnectPermissionResponse(user);
        }
    }

    public void presenterConnectPermission(WebSocketSession session) throws IOException {
        ConferenceUserSession user = viewers.get(session.getId());
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
        ConferenceUserSession user = viewers.get(session.getId());
        if (user == null) {
            ConferenceSender.sendRejectViewerResponse(session, CONFERENCE_USER_NOT_FOUND);
            return;
        }
        newViewer(session, jsonMessage);
    }

    private void rejectPresenter(WebSocketSession session) throws IOException {
        ConferenceUserSession user = viewers.get(session.getId());
        ConferenceSender.sendRejectPresenterResponse(user);
    }

    private void newPresenter(WebSocketSession session, JsonObject jsonMessage)
            throws IOException {
        final String sessionId = session.getId();
        presenter = viewers.get(sessionId);
        viewers.remove(sessionId);
        presenter.setWebRtcEndpoint(new WebRtcEndpoint.Builder(pipeline).build());
        WebRtcEndpoint presenterWebRtc = presenter.getWebRtcEndpoint();
        presenterWebRtc.addIceCandidateFoundListener(
                presenter.getCandidateEventListener()
        );

        String sdpOffer = jsonMessage.getAsJsonPrimitive("sdpOffer").getAsString();
        String sdpAnswer = presenterWebRtc.processOffer(sdpOffer);

        ConferenceSender.sendPresenterResponseSdpAnswer(presenter, sdpAnswer);
        presenterWebRtc.gatherCandidates();
        ConferenceSender.sendNewPresenterForAllViewers(viewers.values(), presenter);
    }

    private void newViewer(final WebSocketSession session, JsonObject jsonMessage)
            throws IOException {
        ConferenceUserSession viewer = viewers.get(session.getId());

        WebRtcEndpoint nextWebRtc = new WebRtcEndpoint.Builder(pipeline).build();
        viewer.setWebRtcEndpoint(nextWebRtc);
        nextWebRtc.addIceCandidateFoundListener(
                viewer.getCandidateEventListener()
        );
        presenter.getWebRtcEndpoint().connect(nextWebRtc);
        String sdpOffer = jsonMessage.getAsJsonPrimitive("sdpOffer").getAsString();
        String sdpAnswer = nextWebRtc.processOffer(sdpOffer);

        ConferenceSender.sendViewerResponseSdpAnswer(viewer, presenter.getUsername(), sdpAnswer);
        nextWebRtc.gatherCandidates();
    }

    public void addIceCandidate(JsonObject jsonMessage, WebSocketSession session) {
        JsonObject candidate = jsonMessage.get("candidate").getAsJsonObject();

        ConferenceUserSession user = getUserBySession(session);
        if (user != null) {
            IceCandidate cand =
                    new IceCandidate(candidate.get("candidate").getAsString(), candidate.get("sdpMid")
                            .getAsString(), candidate.get("sdpMLineIndex").getAsInt());
            user.addCandidate(cand);
        }
    }

    public void sendNewMessage(String messageText, WebSocketSession session) {
        ConferenceUserSession user = getUserBySession(session);
        final ChatMessageDto chatMessage = new ChatMessageDto(
                user.getUuid(),
                user.getUsername(),
                LocalDateTime.now(),
                messageText
        );

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
        ConferenceUserSession leavedUser = viewers.get(sessionId);
        viewers.remove(sessionId);
        List<ConferenceUserSession> allConferenceUsers = new ArrayList<>(viewers.values());
        if (presenter != null) {
            allConferenceUsers.add(presenter);
        }
        ConferenceSender.sendUserLeaveFromConferenceForAllUsers(leavedUser, getAllUsers());
    }

    public synchronized void stop(WebSocketSession session) throws IOException {
        String sessionId = session.getId();
        if (presenter != null && !sessionId.equals(presenter.getSession().getId())) {
            throw new RoomException(USER_ARE_NOT_PRESENTER);
        }

        if (isPresenter(sessionId)) {
            ConferenceSender.sendPresenterStopForAllViewers(presenter.getUsername(), viewers.values());
            for (ConferenceUserSession viewer : viewers.values()) {
                viewer.close();
            }
            viewers.put(session.getId(), presenter);
            presenter.close();
            presenter = null;
        } else if (viewers.containsKey(sessionId)) {
            closePeerConnection(sessionId);
        }
    }

    private void sendAllUsersNewChatMessage(ChatMessageDto chatMessage) {
        final List<ConferenceUserSession> allUsersForSend = new ArrayList<>(viewers.values());
        if (presenter != null) {
            allUsersForSend.add(presenter);
        }
        ConferenceSender.sendAllUsersNewChatMessage(chatMessage, allUsersForSend);
    }

    private ConferenceUserSession getUserBySession(WebSocketSession session) {
        if (presenter != null && presenter.getSession() == session) {
            return presenter;
        }
        return viewers.get(session.getId());
    }

    private boolean isPresenter(String sessionId) {
        return presenter != null && presenter.getSession().getId().equals(sessionId);
    }

    private void closePeerConnection(String sessionId) {
        final ConferenceUserSession user = viewers.get(sessionId);
        if (user != null && user.getWebRtcEndpoint() != null) {
            user.close();
        }
    }

    private String getUserUuid(WebSocketSession session) {
        Optional<CustomUserDetails> optUser = authInfoService.getWebsocketUser(session);
        return optUser
                .map(customUserDetails -> customUserDetails.getUuid().toString())
                .orElseGet(() -> UUID.randomUUID().toString());
    }

    private JsonArray getConferenceUsersNames() {
        List<ConferenceUserSession> allConferenceUsers = getAllUsers();

        JsonArray allConferenceUsersJson = new JsonArray();
        allConferenceUsers
                .forEach(user -> {
                    final JsonObject userJson = new JsonObject();
                    userJson.addProperty("uuid", user.getUuid());
                    userJson.addProperty("name", user.getUsername());

                    allConferenceUsersJson.add(userJson);
                });

        return allConferenceUsersJson;
    }

    private List<ConferenceUserSession> getAllUsers() {
        final List<ConferenceUserSession> allConferenceUsers = new ArrayList<>(viewers.values());
        if (presenter != null) {
            allConferenceUsers.add(presenter);
        }
        return allConferenceUsers;
    }

    public boolean isEmpty() {
        return viewers.isEmpty() && presenter == null;
    }

    @Override
    public void close() {
        executor.submit(messageBuffer::deleteAllMessages);
        pipeline.release();
        if (presenter != null) {
            presenter.close();
        }
        for (ConferenceUserSession user : viewers.values()) {
            user.close();
        }
    }
}