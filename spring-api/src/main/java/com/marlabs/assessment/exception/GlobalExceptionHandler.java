package com.marlabs.assessment.exception;

import com.marlabs.assessment.model.ApiModels.ErrorBody;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(ApiException.class)
  ResponseEntity<ErrorBody> handle(ApiException e){ return ResponseEntity.status(e.status()).body(new ErrorBody(e.code(), e.getMessage())); }
  @ExceptionHandler(Exception.class)
  ResponseEntity<ErrorBody> handleUnexpected(Exception e){ return ResponseEntity.internalServerError().body(new ErrorBody("INTERNAL_ERROR","Request could not be processed")); }
}
