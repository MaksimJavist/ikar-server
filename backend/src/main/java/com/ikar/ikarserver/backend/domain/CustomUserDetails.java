package com.ikar.ikarserver.backend.domain;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

@Getter
public class CustomUserDetails extends User {

    private final Long id;

    private final String uuid;

    private final String firstName;

    private final String secondName;

    public CustomUserDetails(String username,
                             String password, Collection<? extends GrantedAuthority> authorities,
                             Long id,
                             String uuid,
                             String firstName,
                             String secondName) {
        super(username, password, authorities);
        this.id = id;
        this.uuid = uuid;
        this.firstName = firstName;
        this.secondName = secondName;
    }
}
