package com.ikar.ikarserver.backend.service.impl;

import com.ikar.ikarserver.backend.domain.entity.AuthUserInfo;
import com.ikar.ikarserver.backend.domain.entity.CustomUserDetails;
import com.ikar.ikarserver.backend.service.AuthInfoService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

public class AuthInfoServiceImpl implements AuthInfoService {

    @Override
    public AuthUserInfo getAuthUserInfo() {
        User user = getAuthenticationUser();
        return convertToAuthInfo(user);
    }

    private User getAuthenticationUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private AuthUserInfo convertToAuthInfo(User user) {
        CustomUserDetails customUser = (CustomUserDetails) user;

        AuthUserInfo info = new AuthUserInfo();
        info.setId(customUser.getId());
        info.setUsername(customUser.getUsername());

        return info;
    }
}
