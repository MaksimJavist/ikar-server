package com.ikar.ikarserver.backend.service.impl;

import com.ikar.ikarserver.backend.domain.entity.User;
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
    public User register(@NonNull User user) {
        if (existsByUsername(user.getUsername())) {
            throw CreationException.supplier(Messages.BUSY_USERNAME, user.getUsername()).get();
        }
        user.setPassword(
                passwordEncoder.encode(user.getPassword())
        );
        return repository.save(user);
    }

    @Override
    public User update(@NonNull User user) {
        String username = user.getUsername();
        Optional<User> optionalUser = getUserByUsername(username);
        User userForUpdate = optionalUser.orElseThrow(
                NotFoundException.supplier(Messages.NOT_FOUND_USER, username)
        );
        return repository.save(userForUpdate);
    }

    @Override
    public Optional<User> getUserByUsername(@NonNull String username) {
        return repository.findByUsername(username);
    }

    @Override
    public boolean existsByUsername(@NonNull String username) {
        return repository.existsByUsername(username);
    }

}
