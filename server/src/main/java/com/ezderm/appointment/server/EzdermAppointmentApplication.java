package com.ezderm.appointment.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.ezderm.appointment")
public class EzdermAppointmentApplication {

  public static void main(String[] args) {
    SpringApplication.run(EzdermAppointmentApplication.class, args);
  }
}
