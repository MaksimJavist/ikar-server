package com.ikar.ikarserver.backend.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ikar.ikarserver.backend.domain.kurento.room.RoomUserSession;
import com.ikar.ikarserver.backend.dto.ChatMessageDto;
import lombok.extern.slf4j.Slf4j;
import org.kurento.client.IceCandidateFoundEvent;
import org.kurento.jsonrpc.JsonUtils;

import java.io.IOException;
import java.util.Collection;

@Slf4j
public final class RoomSender {

    public static void sendNewChatMessageForAllParticipants(ChatMessageDto chatMessage, Collection<RoomUserSession> participants) {
        final JsonObject dataMessage = new JsonObject();
        dataMessage.addProperty("senderUuid", chatMessage.getSenderUuid());
        dataMessage.addProperty("senderName", chatMessage.getSender());
        dataMessage.addProperty("time", chatMessage.getTimeMessage().toString());
        dataMessage.addProperty("text", chatMessage.getMessage());

        final JsonObject newChatMessage = new JsonObject();
        newChatMessage.addProperty("id", "newChatMessage");
        newChatMessage.add("data", dataMessage);

        sendMessageForAllParticipants(newChatMessage, participants);
    }

    public static void sendExistingParticipants(RoomUserSession participant,
                                                JsonArray participantsArray,
                                                JsonArray messages) throws IOException {
        final JsonObject existingParticipantsMsg = new JsonObject();
        existingParticipantsMsg.addProperty("id", "existingParticipants");
        existingParticipantsMsg.addProperty("registeredName", participant.getName());
        existingParticipantsMsg.addProperty("registeredUuid", participant.getUuid());
        existingParticipantsMsg.add("messages", messages);
        existingParticipantsMsg.add("data", participantsArray);

        participant.sendMessage(existingParticipantsMsg);
    }

    public static void sendNewParticipantArrived(RoomUserSession participantArrived,
                                                 Collection<RoomUserSession> participants) throws IOException {
        final JsonObject newParticipantJson = new JsonObject();
        newParticipantJson.addProperty("uuid", participantArrived.getUuid());
        newParticipantJson.addProperty("name", participantArrived.getName());

        final JsonObject newParticipantMsg = new JsonObject();
        newParticipantMsg.addProperty("id", "newParticipantArrived");
        newParticipantMsg.addProperty("message", "Пользователь " + participantArrived.getName() + " поключился к комнате.");
        newParticipantMsg.add("data", newParticipantJson);

        sendMessageForAllParticipants(newParticipantMsg, participants);
    }

    public static void sendIceCandidate(RoomUserSession user, IceCandidateFoundEvent event) throws IOException {
        JsonObject response = new JsonObject();
        response.addProperty("id", "iceCandidate");
        response.addProperty("uuid", user.getUuid());
        response.addProperty("name", user.getName());
        response.add("candidate", JsonUtils.toJsonObject(event.getCandidate()));

        user.sendMessage(response);
    }

    private static void sendMessageForAllParticipants(JsonObject message,
                                                      Collection<RoomUserSession> participants) {
        for (RoomUserSession participant : participants) {
            try {
                participant.sendMessage(message);
            } catch (IOException e) {
                log.debug("ROOM: participant {} could not be notified", participant.getName(), e);
            }
        }
    }

}
