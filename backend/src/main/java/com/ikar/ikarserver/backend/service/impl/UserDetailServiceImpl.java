package com.ikar.ikarserver.backend.service.impl;

import com.ikar.ikarserver.backend.domain.CustomUserDetails;
import com.ikar.ikarserver.backend.domain.entity.AppUser;
import com.ikar.ikarserver.backend.exception.app.NotFoundException;
import com.ikar.ikarserver.backend.service.AppUserService;
import com.ikar.ikarserver.backend.util.Messages;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class UserDetailServiceImpl implements UserDetailsService {

    private final AppUserService appUserService;

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        AppUser appUser = appUserService.getUserByUsername(s)
                .orElseThrow(NotFoundException.supplier(Messages.NOT_FOUND_USER_ERROR, s));
        return new CustomUserDetails(
                appUser.getUsername(),
                appUser.getPassword(),
                new ArrayList<>(),
                appUser.getUuid(),
                appUser.getFirstName(),
                appUser.getSecondName(),
                appUser.getMiddleName()
        );
    }

}
