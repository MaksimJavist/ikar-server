package com.ikar.ikarserver.backend.controller;

import com.ikar.ikarserver.backend.domain.entity.User;
import com.ikar.ikarserver.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/registration")
@RequiredArgsConstructor
public class RegistrationController {

    private final UserService userService;

    @PreAuthorize("isAnonymous()")
    @PostMapping
    public HttpStatus registration(@RequestBody User user) {
        userService.register(user);
        return HttpStatus.OK;
    }

}
