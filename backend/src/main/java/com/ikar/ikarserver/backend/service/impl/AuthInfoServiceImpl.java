package com.ikar.ikarserver.backend.service.impl;

import com.ikar.ikarserver.backend.domain.AuthInfo;
import com.ikar.ikarserver.backend.domain.AuthInfoDetail;
import com.ikar.ikarserver.backend.domain.AuthUserInfo;
import com.ikar.ikarserver.backend.domain.CustomUserDetails;
import com.ikar.ikarserver.backend.service.AuthInfoService;
import lombok.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.Optional;

@Service
public class AuthInfoServiceImpl implements AuthInfoService {

    @Override
    public AuthUserInfo getAuthUserInfo() {
        User user = (User) getAuthentication().getPrincipal();
        return convertToAuthInfo(user);
    }

    @Override
    public AuthInfo getAuthInfo() {
        Authentication authentication = getAuthentication();
        return createAuthInfo(authentication);
    }

    @Override
    public Optional<CustomUserDetails> getWebsocketUser(@NonNull WebSocketSession session) {
        if (session.getPrincipal() == null) {
            return Optional.empty();
        }
        CustomUserDetails details = getCustomUserDetailsFromSocketSession(session);
        return Optional.of(details);
    }

    private AuthInfo createAuthInfo(Authentication authentication) {
        if (isAnonymousUser(authentication)) {
            AuthInfo info = new AuthInfo();
            info.setAuthenticated(false);
            return info;
        }
        AuthUserInfo userInfo = getAuthUserInfo();

        AuthInfoDetail info = new AuthInfoDetail();
        info.setAuthenticated(true);
        info.setFirstName(userInfo.getFirstName());
        info.setSecondName(userInfo.getSecondName());

        return info;
    }

    private Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    private AuthUserInfo convertToAuthInfo(User user) {
        CustomUserDetails customUser = (CustomUserDetails) user;

        AuthUserInfo info = new AuthUserInfo();
        info.setId(customUser.getId());
        info.setUuid(customUser.getUuid());
        info.setUsername(customUser.getUsername());
        info.setFirstName(customUser.getFirstName());
        info.setSecondName(customUser.getSecondName());

        return info;
    }

    private boolean isAnonymousUser(Authentication authentication) {
        return authentication.getPrincipal().equals("anonymousUser");
    }

    private CustomUserDetails getCustomUserDetailsFromSocketSession(WebSocketSession session) {
        return (CustomUserDetails) ((UsernamePasswordAuthenticationToken) session.getPrincipal()).getPrincipal();
    }
}
