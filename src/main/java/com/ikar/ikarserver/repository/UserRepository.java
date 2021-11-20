package com.ikar.ikarserver.repository;

import com.ikar.ikarserver.domain.entity.User;
import lombok.NonNull;

public interface UserRepository {

    User save(@NonNull User user);

    User findByUsername(@NonNull String username);

}
