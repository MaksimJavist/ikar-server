package com.ikar.ikarserver.backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppUserDto {

    private String uuid;

    private String username;

    private String password;

    private String firstName;

    private String secondName;

    private String middleName;

}
