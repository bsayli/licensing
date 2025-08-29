package com.c9.licensing.service.user.operations;

import com.c9.licensing.model.LicenseInfo;
import jakarta.ws.rs.ProcessingException;
import java.util.Optional;

public interface UserRecoverService {

  Optional<LicenseInfo> recoverGetUser(ProcessingException pe, String userId);
}
