package com.ikar.ikarserver.backend.repository.jpa;

import com.ikar.ikarserver.backend.domain.entity.ChatMessage;
import com.ikar.ikarserver.backend.domain.identifier.CallChatMessageId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;

@NoRepositoryBean
public interface ChatMessageJpaRepository<T extends ChatMessage> extends JpaRepository<T, CallChatMessageId> {

    List<T> getAllByCallIdentifierOrderByDateTimeMessageAsc(String roomIdentifier);

}