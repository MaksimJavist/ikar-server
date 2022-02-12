package com.ikar.ikarserver.backend.converter;

import com.ikar.ikarserver.backend.domain.entity.AppUser;
import com.ikar.ikarserver.backend.dto.AppUserShortDto;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class AppUserToShortDtoConvert implements Converter<AppUser, AppUserShortDto> {

    @Override
    public AppUserShortDto convert(AppUser appUser) {
        final AppUserShortDto shortDto = new AppUserShortDto();
        shortDto.setUuid(
                appUser.getUuid().toString()
        );
        shortDto.setUsername(appUser.getUsername());
        shortDto.setFirstName(appUser.getFirstName());
        shortDto.setSecondName(appUser.getSecondName());
        shortDto.setMiddleName(appUser.getMiddleName());

        return shortDto;
    }
}
