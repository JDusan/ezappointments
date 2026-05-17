package com.ezderm.appointment.web.filter;

import com.ezderm.appointment.service.exception.MissingUsernameException;
import com.ezderm.appointment.service.exception.ValidationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Pattern;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class XUsernameFilter extends OncePerRequestFilter {

  private static final String USERNAME_HEADER = "X-Username";
  private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{4,32}$");

  private final HandlerExceptionResolver handlerExceptionResolver;

  XUsernameFilter(
      @Qualifier("handlerExceptionResolver") HandlerExceptionResolver handlerExceptionResolver) {
    this.handlerExceptionResolver = handlerExceptionResolver;
  }

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {
    String username = request.getHeader(USERNAME_HEADER);
    if (username == null || username.isBlank()) {
      response.setStatus(HttpStatus.UNAUTHORIZED.value());
      handlerExceptionResolver.resolveException(
          request, response, null, new MissingUsernameException("Username header missing"));
      return;
    }
    if (!USERNAME_PATTERN.matcher(username).matches()) {
      response.setStatus(HttpStatus.BAD_REQUEST.value());
      handlerExceptionResolver.resolveException(
          request, response, null, new ValidationException("Username header is invalid"));
      return;
    }

    filterChain.doFilter(request, response);
  }
}
