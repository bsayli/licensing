package io.github.bsayli.licensing.sdk.generator.impl;

import static org.junit.jupiter.api.Assertions.*;

import io.github.bsayli.licensing.client.generated.dto.IssueAccessRequest;
import io.github.bsayli.licensing.client.generated.dto.ValidateAccessRequest;
import io.github.bsayli.licensing.sdk.domain.model.SignatureData;
import io.github.bsayli.licensing.sdk.generator.SignatureGenerator;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.Signature;
import java.util.Base64;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("Unit Test: SignatureGeneratorImpl")
class SignatureGeneratorImplTest {

    private static String pkcs8PrivateKeyBase64(KeyPair kp) {
        return Base64.getEncoder().encodeToString(kp.getPrivate().getEncoded());
    }

    private static String b64Sha256(String input) throws Exception {
        byte[] hash = MessageDigest.getInstance("SHA-256").digest(input.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }

    private static KeyPair newEd25519KeyPair() throws Exception {
        KeyPairGenerator g = KeyPairGenerator.getInstance("Ed25519");
        return g.generateKeyPair();
    }

    private static boolean verifyWithPublic(KeyPair kp, String json, byte[] sig) throws Exception {
        Signature verifier = Signature.getInstance("Ed25519");
        verifier.initVerify(kp.getPublic());
        verifier.update(json.getBytes(StandardCharsets.UTF_8));
        return verifier.verify(sig);
    }

    @Test
    @DisplayName("generateForIssue -> produces verifiable Ed25519 signature over canonical JSON")
    void generateForIssue_verifiable() throws Exception {
        KeyPair kp = newEd25519KeyPair();
        SignatureGenerator gen = new SignatureGeneratorImpl(pkcs8PrivateKeyBase64(kp));

        IssueAccessRequest req = new IssueAccessRequest()
                .serviceId("crm")
                .serviceVersion("1.2.3")
                .instanceId("crm~host1~aa:bb")
                .licenseKey("LK_ABC")
                .checksum("chk");

        String sigB64 = gen.generateForIssue(req);
        assertNotNull(sigB64);
        byte[] sigBytes = Base64.getDecoder().decode(sigB64);

        String expectedJson = SignatureData.builder()
                .serviceId("crm")
                .serviceVersion("1.2.3")
                .instanceId("crm~host1~aa:bb")
                .encryptedLicenseKeyHash(b64Sha256("LK_ABC"))
                .build()
                .toJson();

        assertTrue(verifyWithPublic(kp, expectedJson, sigBytes));
    }

    @Test
    @DisplayName("generateForValidate -> produces verifiable Ed25519 signature over canonical JSON")
    void generateForValidate_verifiable() throws Exception {
        KeyPair kp = newEd25519KeyPair();
        SignatureGenerator gen = new SignatureGeneratorImpl(pkcs8PrivateKeyBase64(kp));

        String token = "header.payload.signature";
        ValidateAccessRequest vreq = new ValidateAccessRequest()
                .serviceId("billing")
                .serviceVersion("2.0.0")
                .instanceId("billing~h1~00:11")
                .checksum("zzz");

        String sigB64 = gen.generateForValidate(token, vreq);
        assertNotNull(sigB64);
        byte[] sigBytes = Base64.getDecoder().decode(sigB64);

        String expectedJson = SignatureData.builder()
                .serviceId("billing")
                .serviceVersion("2.0.0")
                .instanceId("billing~h1~00:11")
                .licenseTokenHash(b64Sha256(token))
                .build()
                .toJson();

        assertTrue(verifyWithPublic(kp, expectedJson, sigBytes));
    }

    @Test
    @DisplayName("generateForValidate(null, ...) -> NullPointerException")
    void generateForValidate_nullToken() throws Exception {
        KeyPair kp = newEd25519KeyPair();
        SignatureGenerator gen = new SignatureGeneratorImpl(pkcs8PrivateKeyBase64(kp));
        ValidateAccessRequest vreq = new ValidateAccessRequest()
                .serviceId("crm")
                .serviceVersion("1.0.0")
                .instanceId("i")
                .checksum("c");
        assertThrows(NullPointerException.class, () -> gen.generateForValidate(null, vreq));
    }

    @Test
    @DisplayName("Constructor with non-base64 string -> IllegalArgumentException from Base64 decoder")
    void ctor_invalidKey() {
        assertThrows(IllegalArgumentException.class, () -> new SignatureGeneratorImpl("not-base64"));
    }
}