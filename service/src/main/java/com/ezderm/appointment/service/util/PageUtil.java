package com.ezderm.appointment.service.util;

import com.ezderm.appointment.service.exception.ValidationException;

public final class PageUtil {

  private static final int DEFAULT_PAGE = 0;
  private static final int DEFAULT_SIZE = 20;

  private PageUtil() {}

  public static int requireValidPageOrDefault(Integer page) {
    int normalizedPage = page == null ? DEFAULT_PAGE : page;
    if (normalizedPage < 0) {
      throw new ValidationException("Page must be zero or greater");
    }
    return normalizedPage;
  }

  public static int requireValidSizeOrDefault(Integer size) {
    int normalizedSize = size == null ? DEFAULT_SIZE : size;
    if (normalizedSize < 1) {
      throw new ValidationException("Size must be greater than zero");
    }
    return normalizedSize;
  }
}
