package com.c9.licensing.errors;

import com.c9.licensing.model.LicenseServiceStatus;

public class LicenseServiceUnexpectedException extends RuntimeException implements LicenseServiceException{
    
  	private static final long serialVersionUID = 113475165148992600L;

	public LicenseServiceUnexpectedException(String message) {
  		super(message);
  	}
  	
  	public LicenseServiceUnexpectedException(String message, Throwable e) {
  		super(message,e);
  	}

	public LicenseServiceStatus getStatus() {
		return LicenseServiceStatus.UNKNOWN_ERROR;
	}
}