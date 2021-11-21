package com.ikar.ikarserver.controller;

import com.ikar.ikarserver.domain.entity.User;
import com.ikar.ikarserver.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/registration")
@RequiredArgsConstructor
public class RegistrationController {

    private final UserService userService;

    @PostMapping
    public HttpStatus registration(@RequestBody User user) {
        userService.register(user);
        return HttpStatus.OK;
    }

}
