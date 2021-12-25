package com.ikar.ikarserver.backend.domain.kurento;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ikar.ikarserver.backend.domain.entity.ChatMessage;
import com.ikar.ikarserver.backend.domain.entity.RoomChatMessage;
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
import java.util.stream.Collectors;

@Slf4j
public class Room implements Closeable {

    private final ConcurrentMap<String, UserSession> participants = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final MediaPipeline pipeline;
    private final String uuid;
    private final RoomChatMessageService messageService;
    private final AuthInfoService authInfoService;

    public Room(String roomName, MediaPipeline pipeline, RoomChatMessageService messageService, AuthInfoService authInfoService) {
        this.uuid = roomName;
        this.pipeline = pipeline;
        this.messageService = messageService;
        this.authInfoService = authInfoService;
        log.info("ROOM {} has been created", roomName);
    }

    public String getUuid() {
        return uuid;
    }

    @PreDestroy
    private void shutdown() {
        this.close();
    }

    public UserSession join(String userName, WebSocketSession session) throws IOException {
        log.info("ROOM {}: adding participant {}", this.uuid, userName);
        Optional<String> optUuid = authInfoService.getWebsocketUserUuid(session);
        String uuid = optUuid.orElse(UUID.randomUUID().toString());
        final UserSession participant = new UserSession(uuid, userName, this.uuid, session, this.pipeline);
        joinRoom(participant);
        participants.put(participant.getUuid(), participant);
        sendParticipantNames(participant);
        return participant;
    }

    public void leave(UserSession user) throws IOException {
        log.debug("PARTICIPANT {}: Leaving room {}", user.getName(), this.uuid);
        this.removeParticipant(user.getUuid());
        user.close();
    }

    private Collection<String> joinRoom(UserSession newParticipant) throws IOException {
        final JsonObject newParticipantJson = new JsonObject();
        newParticipantJson.addProperty("uuid", newParticipant.getUuid());
        newParticipantJson.addProperty("name", newParticipant.getName());

        final JsonObject newParticipantMsg = new JsonObject();
        newParticipantMsg.addProperty("id", "newParticipantArrived");
        newParticipantMsg.add("data", newParticipantJson);

        final List<String> participantsList = new ArrayList<>(participants.values().size());
        log.debug("ROOM {}: notifying other participants of new participant {}", uuid,
                newParticipant.getName());

        for (final UserSession participant : participants.values()) {
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
        for (final UserSession participant : participants.values()) {
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

    public void sendParticipantNames(UserSession user) throws IOException {

        final JsonArray participantsArray = new JsonArray();
        for (final UserSession participant : this.getParticipants()) {
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
        existingParticipantsMsg.add("data", participantsArray);
        log.debug("PARTICIPANT {}: sending a list of {} participants", user.getUuid(),
                participantsArray.size());
        user.sendMessage(existingParticipantsMsg);
    }

    public Collection<UserSession> getParticipants() {
        return participants.values();
    }

    public UserSession getParticipant(String uuid) {
        return participants.get(uuid);
    }

    public boolean existParticipant(String uuid) { return participants.containsKey(uuid); }

    private List<ChatMessageDto> getAllRoomMessages() {
        return messageService.getAllMessagesByUuid(uuid)
                .stream().map(this::convertChatMessagesToDto)
                .collect(Collectors.toList());
    }

    @Override
    public void close() {
        for (final UserSession user : participants.values()) {
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
        sendMessageAllParticipants(message);
//        addNewMessageInBuffer(message);
    }

    private ChatMessageDto convertChatMessagesToDto(ChatMessage message) {
            ChatMessageDto messageDto = new ChatMessageDto();
            messageDto.setSenderUuid(message.getSenderUuid());
            messageDto.setSender(message.getSenderUuid());
            messageDto.setTimeMessage(message.getDateTimeMessage());
            messageDto.setMessage(message.getText());
            return messageDto;
    }

    private void sendMessageAllParticipants(ChatMessageDto message) {
        final JsonObject newChatMessage = new JsonObject();
        newChatMessage.addProperty("id", "newChatMessage");
        newChatMessage.addProperty("senderUuid", message.getSenderUuid());
        newChatMessage.addProperty("sender", message.getSender());
        newChatMessage.addProperty("message", message.getMessage());
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

}
