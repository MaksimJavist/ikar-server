package com.ikar.ikarserver.backend.domain;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;
import java.util.UUID;

@Getter
public class CustomUserDetails extends User {

    private final UUID uuid;

    private final String firstName;

    private final String secondName;

    private final String middleName;

    public CustomUserDetails(String username,
                             String password,
                             Collection<? extends GrantedAuthority> authorities,
                             UUID uuid,
                             String firstName,
                             String secondName,
                             String middleName) {
        super(username, password, authorities);
        this.uuid = uuid;
        this.firstName = firstName;
        this.secondName = secondName;
        this.middleName = middleName;
    }
}
