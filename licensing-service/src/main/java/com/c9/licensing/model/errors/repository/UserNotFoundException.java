package com.c9.licensing.model.errors.repository;

import com.c9.licensing.model.UserErrorCode;

public class UserNotFoundException extends RuntimeException implements UserException{

  	private static final long serialVersionUID = 1519155887563189257L;

	public UserNotFoundException(String message) {
  		super(message);
  	}
  	
  	public UserNotFoundException(String message, Throwable e) {
  		super(message,e);
  	}

	public UserErrorCode getErrorCode() {
		return UserErrorCode.USER_NOT_FOUND;
	}
}