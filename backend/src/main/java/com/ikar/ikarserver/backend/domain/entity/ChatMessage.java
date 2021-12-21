package com.ikar.ikarserver.backend.domain.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass
public class ChatMessage {

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
