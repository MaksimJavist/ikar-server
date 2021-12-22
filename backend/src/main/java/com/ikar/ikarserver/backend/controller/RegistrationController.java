package com.ikar.ikarserver.backend.controller;

import com.ikar.ikarserver.backend.domain.entity.AppUser;
import com.ikar.ikarserver.backend.dto.AppUserDto;
import com.ikar.ikarserver.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.ConversionService;
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
    private final ConversionService converter;

    @PreAuthorize("isAnonymous()")
    @PostMapping
    public HttpStatus registration(@RequestBody AppUserDto appUserDto) {
        userService.register(
                converter.convert(appUserDto, AppUser.class)
        );
        return HttpStatus.OK;
    }

}
