package com.ikar.ikarserver.backend.service;

import com.ikar.ikarserver.backend.domain.AuthInfo;
import com.ikar.ikarserver.backend.domain.AuthUserInfo;

public interface AuthInfoService {

    AuthUserInfo getAuthUserInfo();

    AuthInfo getAuthInfo();

}
