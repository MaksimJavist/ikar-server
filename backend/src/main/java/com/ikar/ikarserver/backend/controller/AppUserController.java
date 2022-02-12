package com.ikar.ikarserver.backend.controller;

import com.ikar.ikarserver.backend.domain.PasswordDto;
import com.ikar.ikarserver.backend.domain.entity.AppUser;
import com.ikar.ikarserver.backend.dto.AppUserDto;
import com.ikar.ikarserver.backend.dto.AppUserShortDto;
import com.ikar.ikarserver.backend.service.AppUserService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class AppUserController {

    private final AppUserService appUserService;
    private final ConversionService converter;

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<AppUserShortDto> getUser() {
        return ResponseEntity.ok(
                converter.convert(
                        appUserService.get(),
                        AppUserShortDto.class
                )
        );
    }

    @PreAuthorize("isAnonymous()")
    @PostMapping
    public HttpStatus registration(@NonNull @RequestBody AppUserDto appUserDto) {
        appUserService.register(
                converter.convert(appUserDto, AppUser.class)
        );
        return HttpStatus.OK;
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping
    public ResponseEntity<AppUserShortDto> updateUser(@NonNull @RequestBody AppUserShortDto appUserDto) {
        return ResponseEntity.ok(
                converter.convert(
                        appUserService.update(
                                converter.convert(appUserDto, AppUser.class)
                        ),
                        AppUserShortDto.class
                )
        );
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("password")
    public HttpStatus updateUserPassword(@NonNull @RequestBody PasswordDto passwordDto) {
        appUserService.updatePassword(passwordDto);
        return HttpStatus.OK;
    }

}
