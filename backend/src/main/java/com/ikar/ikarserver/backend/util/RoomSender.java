package com.ikar.ikarserver.backend.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ikar.ikarserver.backend.domain.kurento.room.RoomUserSession;
import com.ikar.ikarserver.backend.dto.ChatMessageDto;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kurento.client.IceCandidateFoundEvent;
import org.kurento.jsonrpc.JsonUtils;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collection;

import static com.ikar.ikarserver.backend.util.Messages.NOT_ACTIVE_PRESENTER;
import static com.ikar.ikarserver.backend.util.Messages.PRESENTER_BUSY;
import static com.ikar.ikarserver.backend.util.Messages.ROOM_NEW_PARTICIPANT_ARRIVED;
import static com.ikar.ikarserver.backend.util.Messages.USER_IS_BROADCASTING;
import static com.ikar.ikarserver.backend.util.Messages.USER_START_PRESENTATION;
import static com.ikar.ikarserver.backend.util.Messages.USER_STOP_PRESENTATION;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
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
        newParticipantMsg.addProperty("message", MessageFormat.format(ROOM_NEW_PARTICIPANT_ARRIVED, participantArrived.getName()));
        newParticipantMsg.add("data", newParticipantJson);

        sendMessageForAllParticipants(newParticipantMsg, participants);
    }

    public static void sendIceCandidate(RoomUserSession user, String uuid, IceCandidateFoundEvent event) throws IOException {
        JsonObject response = new JsonObject();
        response.addProperty("id", "iceCandidate");
        response.addProperty("uuid", user.getUuid());
        response.addProperty("name", user.getName());
        response.add("candidate", JsonUtils.toJsonObject(event.getCandidate()));

        user.sendMessage(response);
    }

    public static void sendAcceptPresenterConnectPermissionResponse(RoomUserSession user) throws IOException {
        JsonObject message = new JsonObject();
        message.addProperty("id", "presenterConnectPermissionResponse");
        message.addProperty("response", "accepted");

        user.sendMessage(message);
    }

    public static void sendRejectPresenterConnectPermissionResponse(RoomUserSession user, String message) throws IOException {
        JsonObject jsonMessage = new JsonObject();
        jsonMessage.addProperty("id", "presenterConnectPermissionResponse");
        jsonMessage.addProperty("response", "reject");
        jsonMessage.addProperty("message", PRESENTER_BUSY);

        user.sendMessage(jsonMessage);
    }

    public static void sendAcceptViewerConnectPermissionResponse(RoomUserSession user, String presenterName) throws IOException {
        JsonObject message = new JsonObject();
        message.addProperty("id", "viewerConnectPermissionResponse");
        message.addProperty("response", "accepted");
        message.addProperty("message", MessageFormat.format(USER_IS_BROADCASTING, presenterName));

        user.sendMessage(message);
    }

    public static void sendRejectViewerConnectPermissionResponse(RoomUserSession user) throws IOException {
        JsonObject message = new JsonObject();
        message.addProperty("id", "viewerConnectPermissionResponse");
        message.addProperty("response", "reject");
        message.addProperty("message", NOT_ACTIVE_PRESENTER);

        user.sendMessage(message);
    }

    public static void sendPresentationIceCandidate(RoomUserSession user, IceCandidateFoundEvent event) throws IOException {
        JsonObject response = new JsonObject();
        response.addProperty("id", "presentationIceCandidate");
        response.add("candidate", JsonUtils.toJsonObject(event.getCandidate()));

        user.sendMessage(response);
    }

    public static void sendViewerAcceptedResponse(RoomUserSession viewer, String sdpAnswer) throws IOException {
        JsonObject response = new JsonObject();
        response.addProperty("id", "viewerResponse");
        response.addProperty("response", "accepted");
        response.addProperty("sdpAnswer", sdpAnswer);

        viewer.sendMessage(response);
    }

    public static void sendViewerRejectedResponse(RoomUserSession viewer, String message) throws IOException {
        JsonObject response = new JsonObject();
        response.addProperty("id", "viewerResponse");
        response.addProperty("response", "reject");
        response.addProperty("message", message);

        viewer.sendMessage(response);
    }

    public static void sendPresenterAcceptedResponse(RoomUserSession presenter, String sdpAnswer) throws IOException {
        JsonObject response = new JsonObject();
        response.addProperty("id", "presenterResponse");
        response.addProperty("response", "accepted");
        response.addProperty("sdpAnswer", sdpAnswer);

        presenter.sendMessage(response);
    }

    public static void sendPresenterRejectedResponse(RoomUserSession presenter, String message) throws IOException {
        JsonObject response = new JsonObject();
        response.addProperty("id", "presenterResponse");
        response.addProperty("response", "rejected");
        response.addProperty("message", message);

        presenter.sendMessage(response);
    }

    public static void sendNewPresenterForAllParticipants(RoomUserSession presenter, Collection<RoomUserSession> participants) {
        JsonObject response = new JsonObject();
        response.addProperty("id", "newPresenter");
        response.addProperty("message", MessageFormat.format(USER_START_PRESENTATION, presenter.getName()));

        sendMessageForAllParticipants(response, participants);
    }

    public static void sendPresenterStopCommunicationForAllParticipants(String presenterName, Collection<RoomUserSession> participants) {
        JsonObject message = new JsonObject();
        message.addProperty("id", "presenterStopCommunication");
        message.addProperty("message", MessageFormat.format(USER_STOP_PRESENTATION, presenterName));

        sendMessageForAllParticipants(message, participants);
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
