package com.marlabs.assessment.exception;
public class ApiException extends RuntimeException {
  private final String code;
  private final int status;
  public ApiException(String code, String message, int status) { super(message); this.code=code; this.status=status; }
  public String code(){return code;} public int status(){return status;}
}
