package com.ikar.ikarserver.backend.service.impl;

import com.ikar.ikarserver.backend.domain.AuthInfo;
import com.ikar.ikarserver.backend.domain.AuthUserInfo;
import com.ikar.ikarserver.backend.domain.entity.AppUser;
import com.ikar.ikarserver.backend.exception.app.CreationException;
import com.ikar.ikarserver.backend.exception.app.NotFoundException;
import com.ikar.ikarserver.backend.exception.app.UpdateException;
import com.ikar.ikarserver.backend.repository.UserRepository;
import com.ikar.ikarserver.backend.service.AuthInfoService;
import com.ikar.ikarserver.backend.service.UserService;
import com.ikar.ikarserver.backend.util.Messages;
import lombok.NonNull;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

import static com.ikar.ikarserver.backend.util.Messages.UPDATES_USER_IS_NOT_AUTHORIZED;

@Service
public class UserServiceImpl implements UserService {

    private final AuthInfoService authInfoService;
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(AuthInfoService authInfoService, UserRepository repository, @Lazy PasswordEncoder passwordEncoder) {
        this.authInfoService = authInfoService;
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
        AuthUserInfo authInfo = authInfoService.getAuthUserInfo();
        if (!authInfo.getUuid().equals(appUser.getUuid())) {
            throw new UpdateException(UPDATES_USER_IS_NOT_AUTHORIZED);
        }

        Optional<AppUser> optUser = getUserByUuid(appUser.getUuid());
        AppUser originAppUser = optUser
                .orElseThrow(NotFoundException.supplier(Messages.NOT_FOUND_USER_ERROR));

        String originUsername = originAppUser.getUsername();
        String newUsername = appUser.getUsername();
        if (!originUsername.equals(newUsername) && existsByUsername(newUsername)) {
            throw UpdateException.supplier(Messages.BUSY_USERNAME_ERROR, appUser.getUsername()).get();
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
