package com.ikar.ikarserver.backend.service.impl;

import com.ikar.ikarserver.backend.domain.entity.AppUser;
import com.ikar.ikarserver.backend.exception.app.CreationException;
import com.ikar.ikarserver.backend.exception.app.NotFoundException;
import com.ikar.ikarserver.backend.repository.UserRepository;
import com.ikar.ikarserver.backend.service.UserService;
import com.ikar.ikarserver.backend.util.Messages;
import lombok.NonNull;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

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
            throw CreationException.supplier(Messages.BUSY_USERNAME_ERROR, appUser.getUsername()).get();
        }
        appUser.setPassword(
                passwordEncoder.encode(appUser.getPassword())
        );
        return repository.save(appUser);
    }

    @Override
    public AppUser update(@NonNull AppUser appUser) {
        Optional<AppUser> optUser = getUserByUuid(appUser.getUuid());
        AppUser originAppUser = optUser
                .orElseThrow(NotFoundException.supplier(Messages.NOT_FOUND_USER_ERROR));

        String originUsername = originAppUser.getUsername();
        String newUsername = appUser.getUsername();
        if (!originUsername.equals(newUsername) && existsByUsername(newUsername)) {
            throw CreationException.supplier(Messages.BUSY_USERNAME_ERROR, appUser.getUsername()).get();
        }

        originAppUser.setUsername(appUser.getUsername());
        originAppUser.setFirstName(appUser.getFirstName());
        originAppUser.setSecondName(appUser.getSecondName());
        originAppUser.setMiddleName(appUser.getMiddleName());
        return repository.save(originAppUser);
    }

    @Override
    public Optional<AppUser> getUserByUuid(@NonNull UUID uuid) {
        return repository.findByUuid(uuid);
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
