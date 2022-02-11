package com.ikar.ikarserver.backend.domain.kurento.room;

import com.google.gson.JsonObject;
import com.ikar.ikarserver.backend.util.RoomSender;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.kurento.client.EventListener;
import org.kurento.client.IceCandidate;
import org.kurento.client.IceCandidateFoundEvent;
import org.kurento.client.MediaPipeline;
import org.kurento.client.WebRtcEndpoint;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Getter
public class RoomUserSession implements Closeable {

    private final String uuid;
    private final String name;
    private final String roomUuid;
    private final ConcurrentMap<String, WebRtcEndpoint> incomingRtcPeer = new ConcurrentHashMap<>();

    private final WebSocketSession session;
    private final MediaPipeline pipeline;
    private final WebRtcEndpoint outgoingRtcPeer;

    @Setter
    private WebRtcEndpoint presenterMediaEndpoint;

    public RoomUserSession(String uuid, final String name, String roomUuid, final WebSocketSession session,
                           MediaPipeline pipeline) {
        this.uuid = uuid;
        this.pipeline = pipeline;
        this.name = name;
        this.session = session;
        this.roomUuid = roomUuid;
        this.outgoingRtcPeer = new WebRtcEndpoint.Builder(pipeline).build();

        this.outgoingRtcPeer.addIceCandidateFoundListener(
                getIceCandidateFoundEventListener(uuid)
        );
    }

    public void receiveVideoFrom(RoomUserSession sender, String sdpOffer) throws IOException {
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
        this.getEndpointForUser(sender).gatherCandidates();
    }

    private WebRtcEndpoint getEndpointForUser(final RoomUserSession sender) {
        if (sender.getUuid().equals(uuid)) {
            log.debug("PARTICIPANT {}: configuring loopback", this.name);
            return outgoingRtcPeer;
        }

        WebRtcEndpoint incoming = incomingRtcPeer.get(sender.getUuid());
        if (incoming == null) {
            incoming = new WebRtcEndpoint.Builder(pipeline).build();
            incoming.addIceCandidateFoundListener(
                    getIceCandidateFoundEventListener(sender.uuid)
            );
            incomingRtcPeer.put(sender.getUuid(), incoming);
        }
        log.debug("PARTICIPANT {}: obtained endpoint for {}", this.name, sender.getUuid());
        sender.getOutgoingRtcPeer().connect(incoming);

        return incoming;
    }

    public void cancelVideoFrom(final String uuid) {
        log.debug("PARTICIPANT {}: canceling video reception from {}", this.uuid, uuid);
        final WebRtcEndpoint incoming = incomingRtcPeer.remove(uuid);
        incoming.release();
    }

    @Override
    public void close() throws IOException {
        log.debug("PARTICIPANT {}: Releasing resources", this.uuid);
        for (final String remoteParticipantUuid : incomingRtcPeer.keySet()) {
            final WebRtcEndpoint ep = this.incomingRtcPeer.get(remoteParticipantUuid);
            ep.release();
        }
        if (presenterMediaEndpoint != null) {
            presenterMediaEndpoint.release();
        }
        outgoingRtcPeer.release();

    }

    public void sendMessage(JsonObject message) throws IOException {
        log.debug("USER {}: Sending message {}", uuid, message);
        synchronized (session) {
            session.sendMessage(new TextMessage(message.toString()));
        }
    }

    public void addCandidate(IceCandidate candidate, String uuid) {
        if (this.uuid.compareTo(uuid) == 0) {
            outgoingRtcPeer.addIceCandidate(candidate);
        } else {
            WebRtcEndpoint webRtc = incomingRtcPeer.get(uuid);
            if (webRtc != null) {
                webRtc.addIceCandidate(candidate);
            }
        }
    }

    public void addPresentationCandidate(IceCandidate candidate) {
        if (presenterMediaEndpoint != null) {
            presenterMediaEndpoint.addIceCandidate(candidate);
        }
    }

    public EventListener<IceCandidateFoundEvent> getPresentationIceCandidateFoundEventListener() {
        return event -> {
            try {
                RoomSender.sendPresentationIceCandidate(this, event);
            } catch (IOException e) {
                log.debug(e.getMessage());
            }
        };
    }

    private EventListener<IceCandidateFoundEvent> getIceCandidateFoundEventListener(String uuid) {
        return event -> {
            try {
                RoomSender.sendIceCandidate(this, uuid, event);
            } catch (IOException e) {
                log.debug(e.getMessage());
            }
        };
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }
        if (!(obj instanceof RoomUserSession)) {
            return false;
        }
        RoomUserSession other = (RoomUserSession) obj;
        boolean eq = uuid.equals(other.uuid);
        eq &= roomUuid.equals(other.roomUuid);
        return eq;
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + uuid.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + roomUuid.hashCode();
        return result;
    }

}