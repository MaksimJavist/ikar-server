package com.ikar.ikarserver.backend.domain.kurento.room;

import com.ikar.ikarserver.backend.domain.entity.RoomChatMessage;
import com.ikar.ikarserver.backend.exception.app.NotFoundException;
import com.ikar.ikarserver.backend.service.AuthInfoService;
import com.ikar.ikarserver.backend.service.CallIdentifierGenerator;
import com.ikar.ikarserver.backend.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kurento.client.KurentoClient;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.ikar.ikarserver.backend.util.Messages.ROOM_NOT_FOUND;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoomManager {

    private final KurentoClient kurento;
    private final CallIdentifierGenerator identifierService;
    private final AuthInfoService authInfoService;
    private final ChatMessageService<RoomChatMessage> service;
    private final ConcurrentMap<String, Room> rooms = new ConcurrentHashMap<>();

    public Collection<Room> getAll() {
        return rooms.values();
    }

    public String createRoom() {
        String roomIdentifier = identifierService.generateIdentifierRoom();
        log.info("Creation room with identifier {}", roomIdentifier);
        Room room = new Room(roomIdentifier, kurento.createMediaPipeline(), service, authInfoService);
        rooms.put(roomIdentifier, room);
        return roomIdentifier;
    }

    public Room getRoom(String identifier) {
        Room room = rooms.get(identifier);
        if (room == null) {
            throw new NotFoundException(ROOM_NOT_FOUND);
        }
        return room;
    }

    public void removeRoom(String identifier) {
        Room removedRoom = rooms.get(identifier);
        removedRoom.close();
        rooms.remove(identifier);
        log.info("Room {} removed and closed", identifier);
    }

}
