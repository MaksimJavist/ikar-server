package com.ikar.ikarserver.backend.domain.kurento;

import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.kurento.client.Continuation;
import org.kurento.client.EventListener;
import org.kurento.client.IceCandidate;
import org.kurento.client.IceCandidateFoundEvent;
import org.kurento.client.MediaPipeline;
import org.kurento.client.WebRtcEndpoint;
import org.kurento.jsonrpc.JsonUtils;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
public class UserSession implements Closeable {

    private final String uuid;
    private final String name;
    private final WebSocketSession session;

    private final MediaPipeline pipeline;

    private final String roomUuid;
    private final WebRtcEndpoint outgoingMedia;
    private final ConcurrentMap<String, WebRtcEndpoint> incomingMedia = new ConcurrentHashMap<>();

    public UserSession(String uuid, final String name, String roomUuid, final WebSocketSession session,
                       MediaPipeline pipeline) {
        this.uuid = uuid;
        this.pipeline = pipeline;
        this.name = name;
        this.session = session;
        this.roomUuid = roomUuid;
        this.outgoingMedia = new WebRtcEndpoint.Builder(pipeline).build();

        this.outgoingMedia.addIceCandidateFoundListener(new EventListener<IceCandidateFoundEvent>() {

            @Override
            public void onEvent(IceCandidateFoundEvent event) {
                JsonObject response = new JsonObject();
                response.addProperty("id", "iceCandidate");
                response.addProperty("uuid", uuid);
                response.addProperty("name", name);
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
    }

    public String getUuid() {
        return uuid;
    }

    public WebRtcEndpoint getOutgoingWebRtcPeer() {
        return outgoingMedia;
    }

    public String getName() {
        return name;
    }

    public WebSocketSession getSession() {
        return session;
    }

    /**
     * The room to which the user is currently attending.
     *
     * @return The room
     */
    public String getRoomUuid() {
        return this.roomUuid;
    }

    public void receiveVideoFrom(UserSession sender, String sdpOffer) throws IOException {
        log.info("USER {}: connecting with {} in room {}", this.name, sender.getUuid(), this.roomUuid);

        log.trace("USER {}: SdpOffer for {} is {}", this.name, sender.getUuid(), sdpOffer);

        final String ipSdpAnswer = this.getEndpointForUser(sender).processOffer(sdpOffer);
        final JsonObject scParams = new JsonObject();
        scParams.addProperty("id", "receiveVideoAnswer");
        scParams.addProperty("uuid", sender.getUuid());
        scParams.addProperty("name", sender.getName());
        scParams.addProperty("sdpAnswer", ipSdpAnswer);

        log.trace("USER {}: SdpAnswer for {} is {}", this.name, sender.getName(), ipSdpAnswer);
        this.sendMessage(scParams);
        log.debug("gather candidates");
        this.getEndpointForUser(sender).gatherCandidates();
    }

    private WebRtcEndpoint getEndpointForUser(final UserSession sender) {
        if (sender.getUuid().equals(uuid)) {
            log.debug("PARTICIPANT {}: configuring loopback", this.name);
            return outgoingMedia;
        }

        log.debug("PARTICIPANT {}: receiving video from {}", this.name, sender.getName());

        WebRtcEndpoint incoming = incomingMedia.get(sender.getUuid());
        if (incoming == null) {
            log.debug("PARTICIPANT {}: creating new endpoint for {}", this.name, sender.getName());
            incoming = new WebRtcEndpoint.Builder(pipeline).build();

            incoming.addIceCandidateFoundListener(new EventListener<IceCandidateFoundEvent>() {

                @Override
                public void onEvent(IceCandidateFoundEvent event) {
                    JsonObject response = new JsonObject();
                    response.addProperty("id", "iceCandidate");
                    response.addProperty("uuid", sender.getUuid());
                    response.addProperty("name", sender.getName());
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

            incomingMedia.put(sender.getUuid(), incoming);
        }

        log.debug("PARTICIPANT {}: obtained endpoint for {}", this.name, sender.getUuid());
        sender.getOutgoingWebRtcPeer().connect(incoming);

        return incoming;
    }

    public void cancelVideoFrom(final UserSession sender) {
        this.cancelVideoFrom(sender.getUuid());
    }

    public void cancelVideoFrom(final String uuid) {
        log.debug("PARTICIPANT {}: canceling video reception from {}", this.uuid, uuid);
        final WebRtcEndpoint incoming = incomingMedia.remove(uuid);

        log.debug("PARTICIPANT {}: removing endpoint for {}", this.uuid, uuid);
        incoming.release(new Continuation<Void>() {
            @Override
            public void onSuccess(Void result) throws Exception {
                log.trace("PARTICIPANT {}: Released successfully incoming EP for {}",
                        UserSession.this.uuid, uuid);
            }

            @Override
            public void onError(Throwable cause) throws Exception {
                log.warn("PARTICIPANT {}: Could not release incoming EP for {}", UserSession.this.uuid,
                        uuid);
            }
        });
    }

    @Override
    public void close() throws IOException {
        log.debug("PARTICIPANT {}: Releasing resources", this.uuid);
        for (final String remoteParticipantUuid : incomingMedia.keySet()) {

            log.trace("PARTICIPANT {}: Released incoming EP for {}", this.uuid, remoteParticipantUuid);

            final WebRtcEndpoint ep = this.incomingMedia.get(remoteParticipantUuid);

            ep.release(new Continuation<Void>() {

                @Override
                public void onSuccess(Void result) throws Exception {
                    log.trace("PARTICIPANT {}: Released successfully incoming EP for {}",
                            UserSession.this.uuid, remoteParticipantUuid);
                }

                @Override
                public void onError(Throwable cause) throws Exception {
                    log.warn("PARTICIPANT {}: Could not release incoming EP for {}", UserSession.this.uuid,
                            remoteParticipantUuid);
                }
            });
        }

        outgoingMedia.release(new Continuation<Void>() {

            @Override
            public void onSuccess(Void result) throws Exception {
                log.trace("PARTICIPANT {}: Released outgoing EP", UserSession.this.name);
            }

            @Override
            public void onError(Throwable cause) throws Exception {
                log.warn("USER {}: Could not release outgoing EP", UserSession.this.name);
            }
        });
    }

    public void sendMessage(JsonObject message) throws IOException {
        log.debug("USER {}: Sending message {}", uuid, message);
        synchronized (session) {
            session.sendMessage(new TextMessage(message.toString()));
        }
    }

    public void addCandidate(IceCandidate candidate, String uuid) {
        if (this.uuid.compareTo(uuid) == 0) {
            outgoingMedia.addIceCandidate(candidate);
        } else {
            WebRtcEndpoint webRtc = incomingMedia.get(uuid);
            if (webRtc != null) {
                webRtc.addIceCandidate(candidate);
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }
        if (obj == null || !(obj instanceof UserSession)) {
            return false;
        }
        UserSession other = (UserSession) obj;
        boolean eq = uuid.equals(other.uuid);
        eq &= roomUuid.equals(other.roomUuid);
        return eq;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + uuid.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + roomUuid.hashCode();
        return result;
    }

}
