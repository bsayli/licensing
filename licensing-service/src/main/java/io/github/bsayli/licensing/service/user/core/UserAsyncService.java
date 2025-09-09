package io.github.bsayli.licensing.service.user.core;

import io.github.bsayli.licensing.domain.model.LicenseInfo;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface UserAsyncService {

  CompletableFuture<Optional<LicenseInfo>> getUser(String userId);
}
