package com.ikar.ikarserver.backend.domain.entity;

import com.ikar.ikarserver.backend.domain.identifier.RoomChatMessageId;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

@Getter
@Setter
@Entity
@Table(name = "room_chat_messages")
@IdClass(RoomChatMessageId.class)
public class RoomChatMessage extends ChatMessage {

    @Id
    @NotBlank
    @Column(name = "room_identifier")
    private String roomIdentifier;

}
