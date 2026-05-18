package com.ezderm.appointment.web.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiErrorResponse(String title, int status, String detail, String instance) {}
