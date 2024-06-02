package com.c9.licensing.errors;

import com.c9.licensing.model.LicenseErrorCode;

public interface LicenseException {
    LicenseErrorCode getErrorCode();
}
