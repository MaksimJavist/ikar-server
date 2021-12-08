package com.ikar.ikarserver.backend.service.impl;

import com.ikar.ikarserver.backend.service.RoomIdentifierService;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.impl.auth.DigestScheme;
import org.springframework.security.core.context.SecurityContextHolder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;

public final class RoomIdentifierServiceImpl implements RoomIdentifierService {

    private final String HEX_FORMAT = "%02x";

    public String generateIdentifierRoom() {
        MessageDigest md = getDigest();
        byte[] hashingBytes = md.digest(generateBytesByHashing());

        return bytesToHex(hashingBytes);
    }

    private byte[] generateBytesByHashing() {
        String stringByHashing = createStringIdentifier();
        return stringByHashing.getBytes(StandardCharsets.UTF_8);
    }

    private String createStringIdentifier() {
        long unixTime = Instant.now().getEpochSecond();
        String randomString = DigestScheme.createCnonce();
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        return unixTime + randomString + username;
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format(HEX_FORMAT, b));
        }
        return sb.toString();
    }

    private MessageDigest getDigest() {
        return DigestUtils.getSha256Digest();
    }

}
