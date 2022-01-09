package com.ikar.ikarserver.backend.domain.kurento.newconference;

import com.ikar.ikarserver.backend.domain.kurento.conference.Conference;
import com.ikar.ikarserver.backend.service.AuthInfoService;
import com.ikar.ikarserver.backend.service.CallIdentifierGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kurento.client.KurentoClient;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class NewConferenceManager {

    private final CallIdentifierGenerator identifierService;
    private final AuthInfoService authInfoService;
    private final KurentoClient kurentoClient;
    private final ConcurrentMap<String, NewConference> conferences = new ConcurrentHashMap<>();

    public String createConference() {
        final String identifier = identifierService.generateIdentifierRoom();
        log.info("Creation conference with identifier {}", identifier);
        final NewConference conference = new NewConference(identifier, kurentoClient);
        conferences.put(identifier, conference);
        return identifier;
    }

    public Optional<NewConference> getConference(String identifier) {
        log.debug("Searching for conference {}", identifier);
        final NewConference conference = conferences.get(identifier);
        return Optional.ofNullable(conference);
    }

    public void removeConference(NewConference conference) throws IOException {
        conferences.remove(conference.getIdentifier());
        log.info("Conference {} removed and closed", conference.getIdentifier());
    }

}
