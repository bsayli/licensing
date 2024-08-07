package com.c9.licensing.sdk.exception;

public class LicenseServiceUnhandledErrorException extends RuntimeException {
	
	private static final long serialVersionUID = 230784225366822887L;

	public LicenseServiceUnhandledErrorException(String message) {
        super(message);
    }
	
	public LicenseServiceUnhandledErrorException(String message, Throwable e) {
		super(message, e);
	}
	
}
