package com.c9.licensing.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.c9.licensing.model.LicenseErrorCode;
import com.c9.licensing.response.LicenseValidationResponse;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

@ControllerAdvice
public class LicenseControllerAdvice {
	
	@ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<LicenseValidationResponse> handleConstraintViolation(ConstraintViolationException ex) {
        List<String> validationErrors = new ArrayList<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            // Improve error message clarity (optional)
            String userMessage = violation.getMessage();
            userMessage = userMessage.replace(".validatedValue", ""); // Remove technical details (optional)
            validationErrors.add(userMessage);
        }
       
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new LicenseValidationResponse.Builder()
                        .success(false)
                        .errorCode(LicenseErrorCode.INVALID_REQUEST_PARAM.name())
                        .message("Invalid Request Params!")
                        .errorDetails(validationErrors)
                        .build());
    }

}
