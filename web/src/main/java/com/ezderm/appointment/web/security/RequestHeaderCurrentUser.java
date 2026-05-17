package com.ezderm.appointment.web.security;

import com.ezderm.appointment.service.security.CurrentUser;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component
@RequestScope
class RequestHeaderCurrentUser implements CurrentUser {

  private static final String USERNAME_HEADER = "X-Username";

  private final HttpServletRequest request;

  RequestHeaderCurrentUser(HttpServletRequest request) {
    this.request = request;
  }

  @Override
  public String username() {
    return request.getHeader(USERNAME_HEADER);
  }
}
