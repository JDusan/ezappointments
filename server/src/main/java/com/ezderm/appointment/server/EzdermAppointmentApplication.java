package com.ezderm.appointment.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.ezderm.appointment")
@EntityScan("com.ezderm.appointment.repository.entity")
@EnableJpaRepositories("com.ezderm.appointment.repository.jpa")
public class EzdermAppointmentApplication {

  public static void main(String[] args) {
    SpringApplication.run(EzdermAppointmentApplication.class, args);
  }
}
