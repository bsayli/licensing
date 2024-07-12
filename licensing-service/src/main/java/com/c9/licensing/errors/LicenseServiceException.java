package com.c9.licensing.errors;

import com.c9.licensing.model.LicenseErrorCode;

public class LicenseServiceException extends RuntimeException implements LicenseException{
    
  	private static final long serialVersionUID = 113475165148992600L;

	public LicenseServiceException(String message) {
  		super(message);
  	}
  	
  	public LicenseServiceException(String message, Throwable e) {
  		super(message,e);
  	}

	public LicenseErrorCode getErrorCode() {
		return LicenseErrorCode.UNKNOWN_ERROR;
	}
}