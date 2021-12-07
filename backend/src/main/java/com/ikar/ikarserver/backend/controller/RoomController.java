package com.ikar.ikarserver.backend.controller;

import com.ikar.ikarserver.backend.domain.kurento.RoomManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/room")
public class RoomController {

    private final RoomManager roomManager;

    @GetMapping(value = "/create")
    public ResponseEntity<String> createNewRoom(){
        return ResponseEntity.ok(
                roomManager.createRoom()
        );
    }

}
