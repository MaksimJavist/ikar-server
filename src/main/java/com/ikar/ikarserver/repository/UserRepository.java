package com.ikar.ikarserver.repository;

import com.ikar.ikarserver.domain.entity.User;
import lombok.NonNull;

import java.util.Optional;

public interface UserRepository {

    User save(@NonNull User user);

    Optional<User> findByUsername(@NonNull String username);

    boolean existsByUsername(@NonNull String username);

}
