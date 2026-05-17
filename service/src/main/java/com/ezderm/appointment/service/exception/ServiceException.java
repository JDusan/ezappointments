package com.ezderm.appointment.service.exception;

public abstract class ServiceException extends RuntimeException {

  protected ServiceException(String message) {
    super(message);
  }
}
