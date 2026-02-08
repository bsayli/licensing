package io.github.bsayli.licensing.agent.common.exception;

public abstract class LicensingAgentException extends RuntimeException {
    protected LicensingAgentException(String message) {
        super(message);
    }

    protected LicensingAgentException(String message, Throwable cause) {
        super(message, cause);
    }
}
