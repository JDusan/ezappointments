package com.ezderm.appointment.service.exception;

public class MissingUsernameException extends RuntimeException {

  public MissingUsernameException(String message) {
    super(message);
  }
}
