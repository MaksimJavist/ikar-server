package com.ikar.ikarserver.backend.domain.kurento.room;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ikar.ikarserver.backend.domain.CustomUserDetails;
import com.ikar.ikarserver.backend.dto.ChatMessageDto;
import com.ikar.ikarserver.backend.service.AuthInfoService;
import com.ikar.ikarserver.backend.service.RoomChatMessageService;
import lombok.extern.slf4j.Slf4j;
import org.kurento.client.Continuation;
import org.kurento.client.MediaPipeline;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.PreDestroy;
import java.io.Closeable;
import java.io.IOException;
import java.util.*;
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
    private final String uuid;
    private final AuthInfoService authInfoService;

    public Room(String roomName, MediaPipeline pipeline, RoomChatMessageService messageService, AuthInfoService authInfoService) {
        this.uuid = roomName;
        this.pipeline = pipeline;
        this.authInfoService = authInfoService;
        this.messageBuffer = new RoomMessagesBuffer(messageService, uuid);
        log.info("ROOM {} has been created", roomName);
    }

    public String getUuid() {
        return uuid;
    }

    @PreDestroy
    private void shutdown() {
        this.close();
    }

    public RoomUserSession join(String userName, WebSocketSession session) throws IOException {
        log.info("ROOM {}: adding participant {}", this.uuid, userName);
        String uuid = getUserUuid(session);
        final RoomUserSession participant = new RoomUserSession(uuid, userName, this.uuid, session, this.pipeline);
        joinRoom(participant);
        participants.put(participant.getUuid(), participant);
        sendParticipantNames(participant);
        return participant;
    }

    public void leave(RoomUserSession user) throws IOException {
        log.debug("PARTICIPANT {}: Leaving room {}", user.getName(), this.uuid);
        this.removeParticipant(user.getUuid());
        user.close();
    }

    private Collection<String> joinRoom(RoomUserSession newParticipant) throws IOException {
        final JsonObject newParticipantJson = new JsonObject();
        newParticipantJson.addProperty("uuid", newParticipant.getUuid());
        newParticipantJson.addProperty("name", newParticipant.getName());

        final JsonObject newParticipantMsg = new JsonObject();
        newParticipantMsg.addProperty("id", "newParticipantArrived");
        newParticipantMsg.add("data", newParticipantJson);

        final List<String> participantsList = new ArrayList<>(participants.values().size());
        log.debug("ROOM {}: notifying other participants of new participant {}", uuid,
                newParticipant.getName());

        for (final RoomUserSession participant : participants.values()) {
            try {
                participant.sendMessage(newParticipantMsg);
            } catch (final IOException e) {
                log.debug("ROOM {}: participant {} could not be notified", uuid, participant.getName(), e);
            }
            participantsList.add(participant.getUuid());
        }

        return participantsList;
    }

    private void removeParticipant(String uuid) throws IOException {
        participants.remove(uuid);

        log.debug("ROOM {}: notifying all users that {} is leaving the room", this.uuid, uuid);

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
            log.debug("ROOM {}: The users {} could not be notified that {} left the room", this.uuid,
                    unnotifiedParticipants, this.uuid);
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
        final JsonObject existingParticipantsMsg = new JsonObject();
        existingParticipantsMsg.addProperty("id", "existingParticipants");
        existingParticipantsMsg.addProperty("registeredName", user.getName());
        existingParticipantsMsg.addProperty("registeredUuid", user.getUuid());
        existingParticipantsMsg.add("messages", getAllRoomMessages());
        existingParticipantsMsg.add("data", participantsArray);
        log.debug("PARTICIPANT {}: sending a list of {} participants", user.getUuid(),
                participantsArray.size());
        user.sendMessage(existingParticipantsMsg);
    }

    public Collection<RoomUserSession> getParticipants() {
        return participants.values();
    }

    public RoomUserSession getParticipant(String uuid) {
        return participants.get(uuid);
    }

    public boolean existParticipant(String uuid) { return participants.containsKey(uuid); }

    @Override
    public void close() {
        for (final RoomUserSession user : participants.values()) {
            try {
                user.close();
            } catch (IOException e) {
                log.debug("ROOM {}: Could not invoke close on participant {}", this.uuid, user.getName(),
                        e);
            }
        }

        participants.clear();

        pipeline.release(new Continuation<Void>() {

            @Override
            public void onSuccess(Void result) throws Exception {
                log.trace("ROOM {}: Released Pipeline", Room.this.uuid);
            }

            @Override
            public void onError(Throwable cause) throws Exception {
                log.warn("PARTICIPANT {}: Could not release Pipeline", Room.this.uuid);
            }
        });

        log.debug("Room {} closed", this.uuid);
    }

    public void sendNewMessage(ChatMessageDto message) {
        executor.submit(
                () -> messageBuffer.addNewMessageInBuffer(message)
        );
        sendMessageAllParticipants(message);
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

    private void sendMessageAllParticipants(ChatMessageDto message) {
        final JsonObject dataMessage = new JsonObject();
        dataMessage.addProperty("senderUuid", message.getSenderUuid());
        dataMessage.addProperty("senderName", message.getSender());
        dataMessage.addProperty("time", message.getTimeMessage().toString());
        dataMessage.addProperty("text", message.getMessage());

        final JsonObject newChatMessage = new JsonObject();
        newChatMessage.addProperty("id", "newChatMessage");
        newChatMessage.add("data", dataMessage);
        participants.values().forEach(
                participant -> {
                    try {
                        participant.sendMessage(newChatMessage);
                    } catch (IOException e) {
                        log.warn("ERROR SEND MESSAGE: \"{}\" from {}", message.getMessage(), message.getSender());
                    }
                }
        );
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
