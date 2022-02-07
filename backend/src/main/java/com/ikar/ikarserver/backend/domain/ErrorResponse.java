package com.ikar.ikarserver.backend.domain;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.text.MessageFormat;

@Getter
@RequiredArgsConstructor
public class ErrorResponse {

    private final String message;

    public static ErrorResponse of(@NonNull String message, Object... objects) {
        return new ErrorResponse(MessageFormat.format(message, objects));
    }

}
