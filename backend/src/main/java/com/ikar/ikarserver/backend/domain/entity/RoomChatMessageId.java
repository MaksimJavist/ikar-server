package com.ikar.ikarserver.backend.domain.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class RoomChatMessageId implements Serializable {

    private String uuid;

    private String roomIdentifier;

}
