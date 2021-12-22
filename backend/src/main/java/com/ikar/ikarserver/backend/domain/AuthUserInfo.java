package com.ikar.ikarserver.backend.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthUserInfo {

    private Long id;

    private String uuid;

    private String username;

    private String firstName;

    private String secondName;

}