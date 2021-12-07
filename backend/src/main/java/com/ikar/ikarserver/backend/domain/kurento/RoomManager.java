package com.ikar.ikarserver.backend.domain.kurento;

import com.ikar.ikarserver.backend.service.RoomIdentifierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kurento.client.KurentoClient;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@RequiredArgsConstructor
public class RoomManager {

    private final KurentoClient kurento;
    private final RoomIdentifierService identifierService;
    private final ConcurrentMap<String, Room> rooms = new ConcurrentHashMap<>();

    public String createRoom() {
        String roomIdentifier = identifierService.generateIdentifierRoom();
        log.debug("Creation room with identifier {}", roomIdentifier);
        Room room = new Room(roomIdentifier, kurento.createMediaPipeline());
        rooms.put(roomIdentifier, room);
        return roomIdentifier;
    }

    public Room getRoom(String roomName) {
        log.debug("Searching for room {}", roomName);
        Room room = rooms.get(roomName);

        if (room == null) {
            log.debug("Room {} not existent. Will create now!", roomName);
            room = new Room(roomName, kurento.createMediaPipeline());
            rooms.put(roomName, room);
        }
        log.debug("Room {} found!", roomName);
        return room;
    }

    public void removeRoom(Room room) {
        this.rooms.remove(room.getName());
        room.close();
        log.info("Room {} removed and closed", room.getName());
    }

}
