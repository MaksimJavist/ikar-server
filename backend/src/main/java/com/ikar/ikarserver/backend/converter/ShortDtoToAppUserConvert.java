package com.ikar.ikarserver.backend.converter;

import com.ikar.ikarserver.backend.domain.entity.AppUser;
import com.ikar.ikarserver.backend.dto.AppUserShortDto;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ShortDtoToAppUserConvert implements Converter<AppUserShortDto, AppUser> {

    @Override
    public AppUser convert(AppUserShortDto shortDto) {
        final AppUser appUser = new AppUser();
        appUser.setUuid(
                UUID.fromString(shortDto.getUuid())
        );
        appUser.setUsername(shortDto.getUsername());
        appUser.setFirstName(shortDto.getFirstName());
        appUser.setSecondName(shortDto.getSecondName());
        appUser.setMiddleName(shortDto.getMiddleName());
        return appUser;
    }
}
