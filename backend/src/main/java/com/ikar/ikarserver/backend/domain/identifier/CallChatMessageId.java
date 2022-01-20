package com.ikar.ikarserver.backend.domain.identifier;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class CallChatMessageId implements Serializable {

    private String uuid;

    private String callIdentifier;

}
