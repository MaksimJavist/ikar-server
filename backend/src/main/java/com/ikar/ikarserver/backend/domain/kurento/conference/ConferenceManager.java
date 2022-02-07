package com.ikar.ikarserver.backend.domain.kurento.conference;

import com.ikar.ikarserver.backend.domain.entity.ConferenceChatMessage;
import com.ikar.ikarserver.backend.service.AuthInfoService;
import com.ikar.ikarserver.backend.service.CallIdentifierGenerator;
import com.ikar.ikarserver.backend.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kurento.client.KurentoClient;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConferenceManager {

    private final CallIdentifierGenerator identifierService;
    private final AuthInfoService authInfoService;
    private final KurentoClient kurentoClient;
    private final ChatMessageService<ConferenceChatMessage> messageService;
    private final ConcurrentMap<String, Conference> conferences = new ConcurrentHashMap<>();

    public Collection<Conference> getAll() {
        return conferences.values();
    }

    public String createConference() {
        final String identifier = identifierService.generateIdentifierRoom();
        log.info("Creation conference with identifier {}", identifier);
        final Conference conference = new Conference(identifier, authInfoService, messageService, kurentoClient.createMediaPipeline());
        conferences.put(identifier, conference);
        return identifier;
    }

    public Optional<Conference> getConference(String identifier) {
        final Conference conference = conferences.get(identifier);
        return Optional.ofNullable(conference);
    }

    public void removeConference(String identifier) {
        Conference removedConference = conferences.get(identifier);
        removedConference.close();
        conferences.remove(identifier);
        log.info("Conference {} removed and closed", identifier);
    }

}
