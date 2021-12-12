package com.ikar.ikarserver.backend.controller;

import com.ikar.ikarserver.backend.domain.AuthInfo;
import com.ikar.ikarserver.backend.service.AuthInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthInfoController {

    private final AuthInfoService service;

    @GetMapping("info")
    @PreAuthorize("permitAll()")
    public ResponseEntity<AuthInfo> getAuthInfo() {
        return ResponseEntity.ok(
                service.getAuthInfo()
        );
    }

}