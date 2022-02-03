package com.ikar.ikarserver.backend.scheduler;

import com.ikar.ikarserver.backend.domain.kurento.conference.Conference;
import com.ikar.ikarserver.backend.domain.kurento.conference.ConferenceManager;
import com.ikar.ikarserver.backend.domain.kurento.room.Room;
import com.ikar.ikarserver.backend.domain.kurento.room.RoomManager;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CallRemoveScheduler {

    private final RoomManager roomManager;
    private final ConferenceManager conferenceManager;

    @Scheduled(cron = "0 0/5 * * * *")
    public void deleteUnusedCalls() {
        deleteUnusedRooms();
        deleteUnusedConferences();
    }

    private void deleteUnusedRooms() {
        final Collection<Room> rooms = roomManager.getAll();
        Set<Room> roomsForDelete =
                rooms.stream()
                        .filter(room -> {
                            LocalDateTime roomDeletedTime = room.getCreatedTime().plusMinutes(5);
                            return LocalDateTime.now().isAfter(roomDeletedTime);
                        })
                        .collect(Collectors.toSet());
        roomsForDelete.forEach(room -> {
            if (room.isEmpty()) {
                roomManager.removeRoom(room.getIdentifier());
            }
        });
    }

    private void deleteUnusedConferences() {
        final Collection<Conference> conferences = conferenceManager.getAll();
        final Set<Conference> conferencesForRemoved =
                conferences.stream()
                        .filter(conference -> {
                            LocalDateTime conferenceDeleteTime = conference.getCreatedTime().plusMinutes(5);
                            return LocalDateTime.now().isAfter(conferenceDeleteTime);
                        })
                        .collect(Collectors.toSet());
        conferencesForRemoved.forEach(conference -> {
            if (conference.isEmpty()) {
                conferenceManager.removeConference(conference.getIdentifier());
            }
        });
    }

}