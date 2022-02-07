package com.ikar.ikarserver.backend.converter;

import com.ikar.ikarserver.backend.domain.entity.AppUser;
import com.ikar.ikarserver.backend.dto.AppUserDto;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DtoToAppUserConvert implements Converter<AppUserDto, AppUser> {

    @Override
    public AppUser convert(AppUserDto userDto) {
        final AppUser user = new AppUser();
        user.setUsername(userDto.getUsername());
        user.setFirstName(userDto.getFirstName());
        user.setSecondName(userDto.getSecondName());
        user.setMiddleName(userDto.getMiddleName());
        user.setPassword(userDto.getPassword());
        return user;
    }

}
