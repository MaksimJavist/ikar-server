package com.ikar.ikarserver.backend.domain.kurento.room;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ikar.ikarserver.backend.domain.CustomUserDetails;
import com.ikar.ikarserver.backend.dto.ChatMessageDto;
import com.ikar.ikarserver.backend.exception.websocket.RoomException;
import com.ikar.ikarserver.backend.service.AuthInfoService;
import com.ikar.ikarserver.backend.service.RoomChatMessageService;
import com.ikar.ikarserver.backend.util.RoomSender;
import lombok.extern.slf4j.Slf4j;
import org.kurento.client.MediaPipeline;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.PreDestroy;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class Room implements Closeable {

    private final ConcurrentMap<String, RoomUserSession> participants = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final RoomMessagesBuffer messageBuffer;
    private final MediaPipeline pipeline;
    private final String identifier;
    private final AuthInfoService authInfoService;

    public Room(String roomName, MediaPipeline pipeline, RoomChatMessageService messageService, AuthInfoService authInfoService) {
        this.identifier = roomName;
        this.pipeline = pipeline;
        this.authInfoService = authInfoService;
        this.messageBuffer = new RoomMessagesBuffer(messageService, identifier);
        log.info("ROOM {} has been created", roomName);
    }

    public String getIdentifier() {
        return identifier;
    }

    @PreDestroy
    private void shutdown() {
        this.close();
    }

    public RoomUserSession join(String userName, WebSocketSession session) throws IOException {
        String uuid = getUserUuid(session);
        if (participants.containsKey(uuid)) {
            throw new RoomException("Вы уже просматриваете эту конференцию");
        }
        final RoomUserSession participant = new RoomUserSession(uuid, userName, this.identifier, session, this.pipeline);
        joinRoom(participant);
        participants.put(participant.getUuid(), participant);
        sendParticipantNames(participant);
        return participant;
    }

    public void leave(RoomUserSession user) throws IOException {
        log.debug("PARTICIPANT {}: Leaving room {}", user.getName(), this.identifier);
        this.removeParticipant(user.getUuid());
        user.close();
    }

    private void joinRoom(RoomUserSession newParticipant) throws IOException {
        log.debug("ROOM {}: notifying other participants of new participant {}", identifier,
                newParticipant.getName());
        RoomSender.sendNewParticipantArrived(newParticipant, participants.values());
    }

    private void removeParticipant(String uuid) throws IOException {
        participants.remove(uuid);
        log.debug("ROOM {}: notifying all users that {} is leaving the room", this.identifier, uuid);

        final List<String> unnotifiedParticipants = new ArrayList<>();
        final JsonObject participantLeftJson = new JsonObject();
        participantLeftJson.addProperty("id", "participantLeft");
        participantLeftJson.addProperty("uuid", uuid);
        for (final RoomUserSession participant : participants.values()) {
            try {
                participant.cancelVideoFrom(uuid);
                participant.sendMessage(participantLeftJson);
            } catch (final IOException e) {
                unnotifiedParticipants.add(participant.getUuid());
            }
        }

        if (!unnotifiedParticipants.isEmpty()) {
            log.debug("ROOM {}: The users {} could not be notified that {} left the room", this.identifier,
                    unnotifiedParticipants, this.identifier);
        }

    }

    public void sendParticipantNames(RoomUserSession user) throws IOException {
        final JsonArray participantsArray = new JsonArray();
        for (final RoomUserSession participant : this.getParticipants()) {
            if (!participant.equals(user)) {
                final JsonObject participantJson = new JsonObject();
                participantJson.addProperty("uuid", participant.getUuid());
                participantJson.addProperty("name", participant.getName());

                participantsArray.add(participantJson);
            }
        }
        RoomSender.sendExistingParticipants(user, participantsArray, getAllRoomMessages());
    }

    public Collection<RoomUserSession> getParticipants() {
        return participants.values();
    }

    public RoomUserSession getParticipant(String uuid) {
        return participants.get(uuid);
    }

    public boolean existParticipant(String uuid) {
        return participants.containsKey(uuid);
    }

    @Override
    public void close() {
        for (final RoomUserSession user : participants.values()) {
            try {
                user.close();
            } catch (IOException e) {
                log.debug("ROOM {}: Could not invoke close on participant {}", this.identifier, user.getName(), e);
            }
        }
        participants.clear();
        pipeline.release();
        log.debug("Room {} closed", this.identifier);
    }

    public void sendNewMessage(ChatMessageDto message) {
        executor.submit(
                () -> messageBuffer.addNewMessageInBuffer(message)
        );
        RoomSender.sendNewChatMessageForAllParticipants(message, participants.values());
    }

    private JsonArray getAllRoomMessages() {
        List<ChatMessageDto> messages = messageBuffer.getAllRoomMessages();
        JsonArray array = new JsonArray();

        messages.forEach(message -> {
            final JsonObject element = new JsonObject();
            element.addProperty("senderUuid", message.getSenderUuid());
            element.addProperty("senderName", message.getSender());
            element.addProperty("time", message.getTimeMessage().toString());
            element.addProperty("text", message.getMessage());

            array.add(element);
        });

        return array;
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