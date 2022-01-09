package com.ikar.ikarserver.backend.controller;

import com.ikar.ikarserver.backend.domain.kurento.conference.ConferenceManager;
import com.ikar.ikarserver.backend.domain.kurento.newconference.NewConference;
import com.ikar.ikarserver.backend.domain.kurento.newconference.NewConferenceManager;
import com.ikar.ikarserver.backend.domain.kurento.room.RoomManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class CallController {

    private final RoomManager roomManager;
    private final ConferenceManager conferenceManager;
    private final NewConferenceManager newConferenceManager;

    @GetMapping(value = "/room/create")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> createNewRoom(){
        return ResponseEntity.ok(
                roomManager.createRoom()
        );
    }

    @GetMapping(value = "/conference/create")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> createConference() {
        return ResponseEntity.ok(
                conferenceManager.createConference()
        );
    }

    @GetMapping(value = "/newconference/create")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> createNewConference() {
        return ResponseEntity.ok(
                newConferenceManager.createConference()
        );
    }

}
