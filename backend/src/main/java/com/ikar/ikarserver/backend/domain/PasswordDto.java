package com.ikar.ikarserver.backend.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordDto {

    String oldPassword;

    String newPassword;

}
