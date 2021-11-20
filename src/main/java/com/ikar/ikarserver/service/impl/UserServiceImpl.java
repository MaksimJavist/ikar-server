package com.ikar.ikarserver.service.impl;

import com.ikar.ikarserver.domain.entity.User;
import com.ikar.ikarserver.repository.UserRepository;
import com.ikar.ikarserver.service.UserService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private UserRepository repository;
    private PasswordEncoder passwordEncoder;

    @Override
    public User register(@NonNull User user) {

        return null;
    }

    @Override
    public User update(@NonNull User user) {
        return null;
    }

    @Override
    public User getUserByUsername(@NonNull String username) {
        return null;
    }

}
