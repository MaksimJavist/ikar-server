package com.ikar.ikarserver.service;

import com.ikar.ikarserver.domain.entity.User;
import com.ikar.ikarserver.util.Marker;
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
