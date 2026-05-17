package com.ezderm.appointment.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AppointmentDoctorPK implements Serializable {

  @Column(name = "appointment_id", nullable = false)
  private UUID appointmentId;

  @Column(name = "doctor_id", nullable = false)
  private UUID doctorId;

  public AppointmentDoctorPK(UUID appointmentId, UUID doctorId) {
    this.appointmentId = appointmentId;
    this.doctorId = doctorId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof AppointmentDoctorPK that)) {
      return false;
    }
    return Objects.equals(appointmentId, that.appointmentId)
        && Objects.equals(doctorId, that.doctorId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(appointmentId, doctorId);
  }
}
