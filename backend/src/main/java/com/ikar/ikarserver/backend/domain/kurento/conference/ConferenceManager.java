package com.ikar.ikarserver.backend.domain.kurento.conference;

import com.ikar.ikarserver.backend.exception.NotFoundException;
import com.ikar.ikarserver.backend.service.AuthInfoService;
import com.ikar.ikarserver.backend.service.RoomIdentifierGenerator;
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
public class ConferenceManager {

    private final KurentoClient kurentoClient;
    private final RoomIdentifierGenerator identifierService;
    private final AuthInfoService authInfoService;
    private final ConcurrentMap<String, Conference> conferences = new ConcurrentHashMap<>();

    public String createConference() {
        String conferenceIdentifier = identifierService.generateIdentifierRoom();
        log.info("Creation conference with identifier {}", conferenceIdentifier);
        Conference conference = new Conference(authInfoService, conferenceIdentifier, kurentoClient.createMediaPipeline());
        conferences.put(conferenceIdentifier, conference);
        return conferenceIdentifier;
    }

    public Optional<Conference> getConference(String identifier) {
        log.debug("Searching for conference {}", identifier);
        Conference conference = conferences.get(identifier);
        return Optional.ofNullable(conference);
    }

    public void removeConference(Conference conference) throws IOException {
        conferences.remove(conference.getIdentifier());
        conference.close();
        log.info("Conference {} removed and closed", conference.getIdentifier());
    }

}
