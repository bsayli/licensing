package io.github.bsayli.licensing.service.user.operations;

import io.github.bsayli.licensing.model.LicenseInfo;
import jakarta.ws.rs.ProcessingException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface UserAsyncService {

  CompletableFuture<Optional<LicenseInfo>> getUser(String userId);

  CompletableFuture<Optional<LicenseInfo>> recoverGetUser(ProcessingException pe, String userId);
}
