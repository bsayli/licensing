package io.github.bsayli.licensing.sdk.generator.impl;

import static org.junit.jupiter.api.Assertions.*;

import io.github.bsayli.licensing.sdk.api.dto.LicenseAccessRequest;
import io.github.bsayli.licensing.sdk.generator.ClientIdGenerator;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("Unit Test: ClientIdGeneratorImpl")
class ClientIdGeneratorImplTest {

    private final ClientIdGenerator gen = new ClientIdGeneratorImpl();

    private static String expected(String instanceId, String serviceId, String serviceVersion, String checksum) throws Exception {
        char SEP = '\u001F';
        String raw = instanceId + SEP + serviceId + SEP + serviceVersion + SEP + (checksum == null ? "" : checksum);
        byte[] hash = MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
    }

    @Test
    @DisplayName("Deterministic id for same request")
    void deterministic() throws Exception {
        var req = new LicenseAccessRequest("LK_x", "inst-1", "chk-1", "crm", "1.0.0");
        String a = gen.getClientId(req);
        String b = gen.getClientId(req);
        assertEquals(a, b);
        assertEquals(expected("inst-1", "crm", "1.0.0", "chk-1"), a);
    }

    @Test
    @DisplayName("Different inputs produce different ids")
    void differentInputsDifferentIds() {
        var r1 = new LicenseAccessRequest("LK_x", "inst-1", "chk-1", "crm", "1.0.0");
        var r2 = new LicenseAccessRequest("LK_x", "inst-1", "chk-2", "crm", "1.0.0");
        var r3 = new LicenseAccessRequest("LK_x", "inst-1", "chk-1", "crm", "1.1.0");
        String id1 = gen.getClientId(r1);
        String id2 = gen.getClientId(r2);
        String id3 = gen.getClientId(r3);
        assertNotEquals(id1, id2);
        assertNotEquals(id1, id3);
        assertNotEquals(id2, id3);
    }

    @Test
    @DisplayName("Trimming applied to fields")
    void trimming() throws Exception {
        var rTrimmed = new LicenseAccessRequest("LK_x", "inst-1", "chk-1", "crm", "1.0.0");
        var rSpaced  = new LicenseAccessRequest("LK_x", "  inst-1  ", "  chk-1  ", "  crm  ", "  1.0.0  ");
        String a = gen.getClientId(rTrimmed);
        String b = gen.getClientId(rSpaced);
        assertEquals(a, b);
        assertEquals(expected("inst-1", "crm", "1.0.0", "chk-1"), b);
    }

    @Test
    @DisplayName("Null checksum equals empty checksum")
    void nullChecksumEqualsEmpty() {
        var rNull   = new LicenseAccessRequest("LK_x", "inst-1", null, "crm", "1.0.0");
        var rEmpty  = new LicenseAccessRequest("LK_x", "inst-1", "",   "crm", "1.0.0");
        var rSpaces = new LicenseAccessRequest("LK_x", "inst-1", "  ", "crm", "1.0.0");
        String a = gen.getClientId(rNull);
        String b = gen.getClientId(rEmpty);
        String c = gen.getClientId(rSpaces);
        assertEquals(a, b);
        assertEquals(a, c);
    }

    @Test
    @DisplayName("ID is URL-safe Base64 without padding")
    void urlSafeNoPadding() {
        var req = new LicenseAccessRequest("LK_x", "inst-1", "chk-1", "crm", "1.0.0");
        String id = gen.getClientId(req);
        assertTrue(id.matches("^[A-Za-z0-9_-]+$"));
        assertFalse(id.contains("="));
    }
}