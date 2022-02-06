package com.ikar.ikarserver.backend.repository.impl;

import com.ikar.ikarserver.backend.domain.entity.AppUser;
import com.ikar.ikarserver.backend.repository.UserRepository;
import com.ikar.ikarserver.backend.repository.jpa.UserJpaRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository jpaRepository;

    @Override
    public AppUser save(@NonNull AppUser appUser) {
        return jpaRepository.save(appUser);
    }

    @Override
    public Optional<AppUser> findByUuid(@NonNull UUID uuid) {
        return jpaRepository.findById(uuid);
    }

    @Override
    public Optional<AppUser> findByUsername(@NonNull String username) {
        return jpaRepository.findByUsername(username);
    }

    @Override
    public boolean existsByUsername(@NonNull String username) {
        return jpaRepository.existsByUsername(username);
    }

}
