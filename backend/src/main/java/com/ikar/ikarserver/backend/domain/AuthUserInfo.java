package com.ikar.ikarserver.backend.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class AuthUserInfo {

    private UUID uuid;

    private String username;

    private String firstName;

    private String secondName;

    private String middleName;

}