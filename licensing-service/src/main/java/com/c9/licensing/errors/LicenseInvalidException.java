package com.c9.licensing.errors;

import com.c9.licensing.model.LicenseErrorCode;

public class LicenseInvalidException extends RuntimeException implements LicenseException{

    private static final long serialVersionUID = -7371268151798926450L;
    
  	public LicenseInvalidException(String message) {
  		super(message);
  	}
  	
  	public LicenseInvalidException(String message, Throwable e) {
  		super(message,e);
  	}

	public LicenseErrorCode getErrorCode() {
		return LicenseErrorCode.LICENSE_INVALID;
	}
}