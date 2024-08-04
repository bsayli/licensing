package com.c9.licensing.sdk.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.c9.licensing.sdk.exception.LicenseServiceClientErrorException;
import com.c9.licensing.sdk.exception.LicenseServiceServerErrorException;
import com.c9.licensing.sdk.model.LicenseStatus;
import com.c9.licensing.sdk.model.LicenseValidationResponse;
import com.c9.licensing.sdk.model.server.LicenseServerValidationResponse;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

@ControllerAdvice
public class LicenseSdkControllerAdvice {

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<LicenseValidationResponse> handleConstraintViolation(ConstraintViolationException ex) {
		List<String> validationErrors = new ArrayList<>();
		for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
			String path = violation.getPropertyPath().toString();
			String userMessage = violation.getMessage();
			userMessage = userMessage.replace(".validatedValue", ""); // Remove technical details (optional)
			validationErrors.add(String.format("%s: %s", path, userMessage)); // Include parameter name
		}

		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(new LicenseValidationResponse.Builder().success(false)
						.status(LicenseStatus.INVALID_PARAMETER.name())
						.message("Invalid Request Parameters!")
						.errorDetails(validationErrors)
						.build());
	}

	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<LicenseValidationResponse> handleMissingParameter(
			MissingServletRequestParameterException ex) {
		String missingParam = ex.getParameterName();
		String message = String.format("Required request parameter '%s' is missing!", missingParam);

		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(new LicenseValidationResponse.Builder().success(false)
						.status(LicenseStatus.MISSING_PARAMETER.name())
						.message(message)
						.build());
	}

	@ExceptionHandler(MissingRequestHeaderException.class)
	public ResponseEntity<LicenseValidationResponse> handleMissingHeader(MissingRequestHeaderException ex) {
		String missingHeader = ex.getHeaderName();
		String message = String.format("Required request header '%s' is missing!", missingHeader);

		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(new LicenseValidationResponse.Builder().success(false)
						.status(LicenseStatus.MISSING_PARAMETER.name())
						.message(message)
						.build());
	}

	@ExceptionHandler(LicenseServiceClientErrorException.class)
	public ResponseEntity<LicenseValidationResponse> handleInvalidParameter(LicenseServiceClientErrorException ex) {
		LicenseServerValidationResponse serverResponse = ex.getServerResponse();
		String status = serverResponse.status();
		String message = serverResponse.message();
		if (status.contains("TOKEN_")) {
			status = LicenseStatus.INVALID_REQUEST.name();
			message = "License request is invalid";
		}
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body(new LicenseValidationResponse.Builder().success(false).status(status).message(message).build());
	}

	@ExceptionHandler(LicenseServiceServerErrorException.class)
	public ResponseEntity<LicenseValidationResponse> handleInvalidParameter(LicenseServiceServerErrorException ex) {
		LicenseServerValidationResponse serverResponse = ex.getServerResponse();
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(new LicenseValidationResponse.Builder().success(false)
						.status(serverResponse.status())
						.message(serverResponse.message())
						.build());
	}

}
