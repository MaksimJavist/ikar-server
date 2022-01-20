package com.ikar.ikarserver.backend.domain.entity;

import com.ikar.ikarserver.backend.domain.identifier.CallChatMessageId;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass
@IdClass(CallChatMessageId.class)
public class ChatMessage {

    @Id
    @NotBlank
    @Column(name = "uuid")
    private String uuid;

    @Id
    @NotBlank
    @Column(name = "call_identifier")
    private String callIdentifier;

    @NotNull
    @Column(name = "date_time_message")
    private LocalDateTime dateTimeMessage;

    @Size(max = 64)
    @Column(name = "sender_uuid")
    private String senderUuid;

    @Size(max = 64)
    @Column(name = "sender_name")
    private String senderName;

    @NotBlank
    @Column(name = "text_message")
    private String text;

}
