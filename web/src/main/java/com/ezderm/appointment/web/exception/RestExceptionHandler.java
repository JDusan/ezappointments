package com.ezderm.appointment.web.exception;

import com.ezderm.appointment.service.exception.ConflictException;
import com.ezderm.appointment.service.exception.ForbiddenException;
import com.ezderm.appointment.service.exception.MissingUsernameException;
import com.ezderm.appointment.service.exception.NotFoundException;
import com.ezderm.appointment.service.exception.ValidationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
class RestExceptionHandler {

  @ExceptionHandler(ValidationException.class)
  ResponseEntity<ApiErrorResponse> handleValidation(
      ValidationException exception, HttpServletRequest request) {
    return error(HttpStatus.BAD_REQUEST, exception.getLocalizedMessage(), request);
  }

  @ExceptionHandler(NotFoundException.class)
  ResponseEntity<ApiErrorResponse> handleNotFound(
      NotFoundException exception, HttpServletRequest request) {
    return error(HttpStatus.NOT_FOUND, exception.getMessage(), request);
  }

  @ExceptionHandler(ForbiddenException.class)
  ResponseEntity<ApiErrorResponse> handleForbidden(
      ForbiddenException exception, HttpServletRequest request) {
    return error(HttpStatus.FORBIDDEN, exception.getMessage(), request);
  }

  @ExceptionHandler(MissingUsernameException.class)
  ResponseEntity<ApiErrorResponse> handleMissingUsername(
      MissingUsernameException exception, HttpServletRequest request) {
    return error(HttpStatus.UNAUTHORIZED, exception.getMessage(), request);
  }

  @ExceptionHandler(ConflictException.class)
  ResponseEntity<ApiErrorResponse> handleConflict(
      ConflictException exception, HttpServletRequest request) {
    return error(HttpStatus.CONFLICT, exception.getMessage(), request);
  }

  @ExceptionHandler(HandlerMethodValidationException.class)
  ResponseEntity<ApiErrorResponse> handleHandlerMethodValidation(
      HandlerMethodValidationException exception, HttpServletRequest request) {
    return error(HttpStatus.BAD_REQUEST, extractHandlerValidationMessage(exception), request);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(
      MethodArgumentNotValidException exception, HttpServletRequest request) {
    return error(HttpStatus.BAD_REQUEST, extractBodyValidationMessage(exception), request);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  ResponseEntity<ApiErrorResponse> handleHttpMessageNotReadable(
      HttpMessageNotReadableException exception, HttpServletRequest request) {
    return error(HttpStatus.BAD_REQUEST, "Request body is missing or malformed.", request);
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  ResponseEntity<ApiErrorResponse> handleMissingServletRequestParameter(
      MissingServletRequestParameterException exception, HttpServletRequest request) {
    return error(
        HttpStatus.BAD_REQUEST,
        "Required parameter '%s' is missing.".formatted(exception.getParameterName()),
        request);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  ResponseEntity<ApiErrorResponse> handleMethodArgumentTypeMismatch(
      MethodArgumentTypeMismatchException exception, HttpServletRequest request) {
    String parameterName = exception.getName();
    return error(
        HttpStatus.BAD_REQUEST,
        "Parameter '%s' has an invalid value.".formatted(parameterName),
        request);
  }

  @ExceptionHandler(
    {NoHandlerFoundException.class, NoResourceFoundException.class, HttpRequestMethodNotSupportedException.class})
  ResponseEntity<ApiErrorResponse> handleNoHandlerFound(HttpServletRequest request) {
    return error(HttpStatus.NOT_FOUND, "Resource not found.", request);
  }

  @ExceptionHandler(Exception.class)
  ResponseEntity<ApiErrorResponse> handleUnexpected(
      Exception exception, HttpServletRequest request) {
    return error(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", request);
  }

  private ResponseEntity<ApiErrorResponse> error(
      HttpStatus status, String detail, HttpServletRequest request) {
    ApiErrorResponse body =
        new ApiErrorResponse(
            status.getReasonPhrase(), status.value(), detail, request.getRequestURI());
    return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(body);
  }

  /**
   * Converts Spring's method-parameter validation exception into a short readable message. This
   * keeps internal names hidden and returns the request parameter name instead. Examples: "page
   * must be greater than or equal to 0" and "size must be less than or equal to 200".
   */
  private String extractHandlerValidationMessage(HandlerMethodValidationException exception) {
    return exception.getParameterValidationResults().stream()
        .findFirst()
        .map(
            result -> {
              String parameterName = resolveParameterName(result);
              String message =
                  result.getResolvableErrors().stream()
                      .findFirst()
                      .map(
                          error -> {
                            ConstraintViolation<?> violation =
                                result.unwrap(error, ConstraintViolation.class);
                            return violation.getMessage();
                          })
                      .orElse("is invalid");
              return formatValidationMessage(parameterName, message);
            })
        .orElse("Request is invalid.");
  }

  private String resolveParameterName(
      org.springframework.validation.method.ParameterValidationResult result) {
    RequestParam requestParam =
        result.getMethodParameter().getParameterAnnotation(RequestParam.class);
    if (requestParam != null && !requestParam.value().isBlank()) {
      return requestParam.value();
    }

    RequestHeader requestHeader =
        result.getMethodParameter().getParameterAnnotation(RequestHeader.class);
    if (requestHeader != null && !requestHeader.value().isBlank()) {
      return requestHeader.value();
    }

    return result.getMethodParameter().getParameterName();
  }

  private String extractBodyValidationMessage(MethodArgumentNotValidException exception) {
    FieldError fieldError =
        exception.getBindingResult().getFieldErrors().stream().findFirst().orElse(null);
    if (fieldError != null) {
      return formatValidationMessage(fieldError.getField(), fieldError.getDefaultMessage());
    }

    ObjectError objectError =
        exception.getBindingResult().getGlobalErrors().stream().findFirst().orElse(null);
    if (objectError != null && objectError.getDefaultMessage() != null) {
      return objectError.getDefaultMessage();
    }

    return "Request body is invalid.";
  }

  private String formatValidationMessage(String fieldName, String message) {
    if (fieldName == null || fieldName.isBlank()) {
      return message == null || message.isBlank() ? "Request is invalid." : message;
    }
    if (message == null || message.isBlank()) {
      return "%s is invalid.".formatted(fieldName);
    }
    return "%s %s".formatted(fieldName, message);
  }
}
