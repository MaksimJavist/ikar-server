package com.ikar.ikarserver.backend.service;

import com.ikar.ikarserver.backend.domain.entity.User;
import com.ikar.ikarserver.backend.util.Marker;
import lombok.NonNull;
import org.springframework.validation.annotation.Validated;

import javax.validation.groups.Default;
import java.util.Optional;

public interface UserService {

    @Validated({Marker.Create.class, Default.class})
    User register(@NonNull User user);

    @Validated({Marker.Update.class, Default.class})
    User update(@NonNull User user);

    Optional<User> getUserByUsername(@NonNull String username);

    boolean existsByUsername(@NonNull String username);

}
