package com.ezderm.appointment.web.exception;

import com.ezderm.appointment.api.model.ErrorResponse;
import com.ezderm.appointment.service.exception.ConflictException;
import com.ezderm.appointment.service.exception.ForbiddenException;
import com.ezderm.appointment.service.exception.MissingUsernameException;
import com.ezderm.appointment.service.exception.NotFoundException;
import com.ezderm.appointment.service.exception.ValidationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
class RestExceptionHandler {

  @ExceptionHandler(ValidationException.class)
  ResponseEntity<ErrorResponse> handleValidation(
      ValidationException exception, HttpServletRequest request) {
    ErrorResponse body =
        new ErrorResponse("Bad Request", HttpStatus.BAD_REQUEST.value())
            .detail(exception.getLocalizedMessage())
            .instance(request.getRequestURI());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(body);
  }

  @ExceptionHandler(NotFoundException.class)
  ResponseEntity<ErrorResponse> handleNotFound(
      NotFoundException exception, HttpServletRequest request) {
    ErrorResponse body =
        new ErrorResponse("Not Found", HttpStatus.NOT_FOUND.value())
            .detail(exception.getMessage())
            .instance(request.getRequestURI());
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(body);
  }

  @ExceptionHandler(ForbiddenException.class)
  ResponseEntity<ErrorResponse> handleForbidden(
      ForbiddenException exception, HttpServletRequest request) {
    ErrorResponse body =
        new ErrorResponse("Forbidden", HttpStatus.FORBIDDEN.value())
            .detail(exception.getMessage())
            .instance(request.getRequestURI());
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .contentType(MediaType.APPLICATION_JSON)
        .body(body);
  }

  @ExceptionHandler(MissingUsernameException.class)
  ResponseEntity<ErrorResponse> handleMissingUsername(
      MissingUsernameException exception, HttpServletRequest request) {
    ErrorResponse body =
        new ErrorResponse("Unauthorized", HttpStatus.UNAUTHORIZED.value())
            .detail(exception.getMessage())
            .instance(request.getRequestURI());
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .contentType(MediaType.APPLICATION_JSON)
        .body(body);
  }

  @ExceptionHandler(ConflictException.class)
  ResponseEntity<ErrorResponse> handleConflict(
      ConflictException exception, HttpServletRequest request) {
    ErrorResponse body =
        new ErrorResponse("Conflict", HttpStatus.CONFLICT.value())
            .detail(exception.getMessage())
            .instance(request.getRequestURI());
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .contentType(MediaType.APPLICATION_JSON)
        .body(body);
  }

  @ExceptionHandler({
    MethodArgumentNotValidException.class,
    HandlerMethodValidationException.class,
    ConstraintViolationException.class,
    HttpMessageNotReadableException.class,
    MissingRequestHeaderException.class,
    MissingServletRequestParameterException.class,
    MethodArgumentTypeMismatchException.class
  })
  ResponseEntity<ErrorResponse> handleSpringValidation(
      Exception exception, HttpServletRequest request) {
    ErrorResponse body =
        new ErrorResponse("Bad Request", HttpStatus.BAD_REQUEST.value())
            .detail(exception.getMessage())
            .instance(request.getRequestURI());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(body);
  }

  @ExceptionHandler(Exception.class)
  ResponseEntity<ErrorResponse> handleUnexpected(Exception exception, HttpServletRequest request) {
    ErrorResponse body =
        new ErrorResponse("Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR.value())
            .detail("Unexpected error")
            .instance(request.getRequestURI());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .contentType(MediaType.APPLICATION_JSON)
        .body(body);
  }
}
