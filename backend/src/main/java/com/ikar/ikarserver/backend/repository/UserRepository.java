package com.ikar.ikarserver.backend.repository;

import com.ikar.ikarserver.backend.domain.entity.AppUser;
import lombok.NonNull;

import java.util.Optional;

public interface UserRepository {

    AppUser save(@NonNull AppUser appUser);

    Optional<AppUser> findByUsername(@NonNull String username);

    boolean existsByUsername(@NonNull String username);

}
