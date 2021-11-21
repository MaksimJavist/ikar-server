package com.ikar.ikarserver.service.impl;

import com.ikar.ikarserver.domain.entity.User;
import com.ikar.ikarserver.exception.NotFoundException;
import com.ikar.ikarserver.service.UserService;
import com.ikar.ikarserver.util.Messages;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class UserDetailServiceImpl implements UserDetailsService {

    private final UserService userService;

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        User user = userService.getUserByUsername(s)
                .orElseThrow(NotFoundException.supplier(Messages.NOT_FOUND_USER, s));
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(), user.getPassword(), new ArrayList<>()
        );
    }

}
