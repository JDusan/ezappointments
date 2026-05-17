package com.ezderm.appointment.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "appointment_doctor")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AppointmentDoctorEntity {

  @EmbeddedId private AppointmentDoctorId id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @MapsId("appointmentId")
  @JoinColumn(name = "appointment_id", nullable = false)
  private AppointmentEntity appointment;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @MapsId("doctorId")
  @JoinColumn(name = "doctor_id", nullable = false)
  private DoctorEntity doctor;

  @Column(name = "sort_order", nullable = false)
  private int sortOrder;

  public AppointmentDoctorEntity(
      AppointmentEntity appointment, DoctorEntity doctor, int sortOrder) {
    this.appointment = appointment;
    this.doctor = doctor;
    this.sortOrder = sortOrder;
    this.id = new AppointmentDoctorId(appointment.getId(), doctor.getId());
  }
}
