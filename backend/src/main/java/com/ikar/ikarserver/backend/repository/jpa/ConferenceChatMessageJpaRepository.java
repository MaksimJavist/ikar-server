package com.ikar.ikarserver.backend.repository.jpa;

import com.ikar.ikarserver.backend.domain.entity.ConferenceChatMessage;
import com.ikar.ikarserver.backend.domain.identifier.CallChatMessageId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConferenceChatMessageJpaRepository extends JpaRepository<ConferenceChatMessage, CallChatMessageId> {

    List<ConferenceChatMessage> getAllByCallIdentifierOrderByDateTimeMessageAsc(String roomIdentifier);

}
