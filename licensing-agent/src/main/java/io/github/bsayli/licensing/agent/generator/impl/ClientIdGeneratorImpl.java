package io.github.bsayli.licensing.agent.generator.impl;

import io.github.bsayli.licensing.agent.api.dto.LicenseAccessRequest;
import io.github.bsayli.licensing.agent.generator.ClientIdGenerator;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Component
public class ClientIdGeneratorImpl implements ClientIdGenerator {

    private static final String ALGORITHM = "SHA-256";
    private static final char SEP = '\u001F';
    private static final Base64.Encoder B64URL_NOPAD_ENC = Base64.getUrlEncoder().withoutPadding();

    @Override
    public String getClientId(LicenseAccessRequest request) {
        String instanceId = request.instanceId().trim();
        String serviceId = request.serviceId().trim();
        String serviceVersion = request.serviceVersion().trim();
        String checksum = request.checksum() == null ? "" : request.checksum().trim();
        String raw = instanceId + SEP + serviceId + SEP + serviceVersion + SEP + checksum;

        try {
            byte[] hashed =
                    MessageDigest.getInstance(ALGORITHM).digest(raw.getBytes(StandardCharsets.UTF_8));
            return B64URL_NOPAD_ENC.encodeToString(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Hash algorithm not found: " + ALGORITHM, e);
        }
    }
}
