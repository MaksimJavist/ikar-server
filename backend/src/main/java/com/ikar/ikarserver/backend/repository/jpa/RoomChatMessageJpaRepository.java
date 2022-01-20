package com.ikar.ikarserver.backend.repository.jpa;

import com.ikar.ikarserver.backend.domain.entity.RoomChatMessage;
import com.ikar.ikarserver.backend.domain.identifier.CallChatMessageId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomChatMessageJpaRepository extends JpaRepository<RoomChatMessage, CallChatMessageId> {

    List<RoomChatMessage> getAllByCallIdentifierOrderByDateTimeMessageDesc(String roomIdentifier);

}
