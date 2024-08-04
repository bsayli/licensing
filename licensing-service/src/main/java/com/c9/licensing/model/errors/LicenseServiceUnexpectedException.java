package com.c9.licensing.model.errors;

import com.c9.licensing.model.LicenseServiceStatus;

public class LicenseServiceUnexpectedException extends LicenseServiceExceptionImpl {
    
  	private static final long serialVersionUID = 113475165148992600L;

	public LicenseServiceUnexpectedException(String message) {
  		super(message);
  	}
  	
  	public LicenseServiceUnexpectedException(String message, Throwable e) {
  		super(message,e);
  	}

	public LicenseServiceStatus getStatus() {
		return LicenseServiceStatus.INTERNAL_SERVER_ERROR;
	}
}