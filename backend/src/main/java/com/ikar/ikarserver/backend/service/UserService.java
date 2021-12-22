package com.ikar.ikarserver.backend.service;

import com.ikar.ikarserver.backend.domain.entity.AppUser;
import com.ikar.ikarserver.backend.util.Marker;
import lombok.NonNull;
import org.springframework.validation.annotation.Validated;

import javax.validation.groups.Default;
import java.util.Optional;

public interface UserService {

    @Validated({Marker.Create.class, Default.class})
    AppUser register(@NonNull AppUser appUser);

    @Validated({Marker.Update.class, Default.class})
    AppUser update(@NonNull AppUser appUser);

    Optional<AppUser> getUserByUsername(@NonNull String username);

    boolean existsByUsername(@NonNull String username);

}
