package io.github.bsayli.licensing.agent.common.exception;

import org.springframework.http.HttpStatus;

import java.util.List;

public class LicensingAgentRemoteServiceException extends LicensingAgentException {

    private final HttpStatus httpStatus;
    private final String errorCode;
    private final String topMessage;
    private final transient List<String> details;

    public LicensingAgentRemoteServiceException(
            HttpStatus httpStatus, String errorCode, String topMessage, List<String> details) {
        super(topMessage);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
        this.topMessage = topMessage;
        this.details = details;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getTopMessage() {
        return topMessage;
    }

    public List<String> getDetails() {
        return details;
    }
}
