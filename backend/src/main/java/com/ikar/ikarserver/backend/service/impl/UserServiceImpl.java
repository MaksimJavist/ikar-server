package com.ikar.ikarserver.backend.service.impl;

import com.ikar.ikarserver.backend.domain.entity.AppUser;
import com.ikar.ikarserver.backend.exception.CreationException;
import com.ikar.ikarserver.backend.exception.NotFoundException;
import com.ikar.ikarserver.backend.repository.UserRepository;
import com.ikar.ikarserver.backend.service.UserService;
import com.ikar.ikarserver.backend.util.Messages;
import lombok.NonNull;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository repository, @Lazy PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public AppUser register(@NonNull AppUser appUser) {
        if (existsByUsername(appUser.getUsername())) {
            throw CreationException.supplier(Messages.BUSY_USERNAME, appUser.getUsername()).get();
        }
        appUser.setPassword(
                passwordEncoder.encode(appUser.getPassword())
        );
        return repository.save(appUser);
    }

    @Override
    public AppUser update(@NonNull AppUser appUser) {
        String username = appUser.getUsername();
        Optional<AppUser> optionalUser = getUserByUsername(username);
        AppUser appUserForUpdate = optionalUser.orElseThrow(
                NotFoundException.supplier(Messages.NOT_FOUND_USER, username)
        );
        return repository.save(appUserForUpdate);
    }

    @Override
    public Optional<AppUser> getUserByUsername(@NonNull String username) {
        return repository.findByUsername(username);
    }

    @Override
    public boolean existsByUsername(@NonNull String username) {
        return repository.existsByUsername(username);
    }

}
