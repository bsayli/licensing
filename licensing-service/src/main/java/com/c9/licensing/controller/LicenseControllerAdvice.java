package com.c9.licensing.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.c9.licensing.errors.InvalidParameterException;
import com.c9.licensing.model.LicenseServiceStatus;
import com.c9.licensing.response.LicenseValidationResponse;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

@ControllerAdvice
public class LicenseControllerAdvice {

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
						.status(LicenseServiceStatus.INVALID_PARAMETER.name())
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
						.status(LicenseServiceStatus.MISSING_PARAMETER.name())
						.message(message)
						.build());
	}

	@ExceptionHandler(MissingRequestHeaderException.class)
	public ResponseEntity<LicenseValidationResponse> handleMissingHeader(MissingRequestHeaderException ex) {
		String missingHeader = ex.getHeaderName();
		String message = String.format("Required request header '%s' is missing!", missingHeader);

		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(new LicenseValidationResponse.Builder().success(false)
						.status(LicenseServiceStatus.MISSING_PARAMETER.name())
						.message(message)
						.build());
	}

	@ExceptionHandler(InvalidParameterException.class)
	public ResponseEntity<LicenseValidationResponse> handleInvalidParameter(InvalidParameterException ex) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(new LicenseValidationResponse.Builder().success(false)
						.status(ex.getStatus().name())
						.message(ex.getMessage())
						.build());
	}

}
