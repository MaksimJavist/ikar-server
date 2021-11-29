package com.ikar.ikarserver.backend.repository;

import com.ikar.ikarserver.backend.domain.entity.User;
import lombok.NonNull;

import java.util.Optional;

public interface UserRepository {

    User save(@NonNull User user);

    Optional<User> findByUsername(@NonNull String username);

    boolean existsByUsername(@NonNull String username);

}
