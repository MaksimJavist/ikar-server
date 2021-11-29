package com.ikar.ikarserver.backend.repository.impl;

import com.ikar.ikarserver.backend.domain.entity.User;
import com.ikar.ikarserver.backend.repository.UserRepository;
import com.ikar.ikarserver.backend.repository.jpa.UserJpaRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository jpaRepository;

    @Override
    public User save(@NonNull User user) {
        return jpaRepository.save(user);
    }

    @Override
    public Optional<User> findByUsername(@NonNull String username) {
        return jpaRepository.findByUsername(username);
    }

    @Override
    public boolean existsByUsername(@NonNull String username) {
        return jpaRepository.existsByUsername(username);
    }

}
