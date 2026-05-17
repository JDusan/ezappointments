package com.ezderm.appointment.repository.jpa;

import com.ezderm.appointment.repository.entity.AppointmentEntity;
import com.ezderm.appointment.repository.entity.AppointmentStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<AppointmentEntity, UUID> {

  boolean existsByPatient_IdAndStatusAndStartsAtAfter(
      UUID patientId, AppointmentStatus status, Instant startsAt);

  boolean existsByDoctorParticipants_Doctor_IdAndStatusAndStartsAtAfter(
      UUID doctorId, AppointmentStatus status, Instant startsAt);

  @Query(
      """
      select distinct a
      from AppointmentEntity a
      join fetch a.patient
      join fetch a.createdByDoctor
      left join fetch a.doctorParticipants dp
      left join fetch dp.doctor
      where a.id = :id
      """)
  Optional<AppointmentEntity> findByIdWithDetails(@Param("id") UUID id);

  @Query(
      """
      select a.id
      from AppointmentEntity a
      join a.doctorParticipants dp
      where dp.doctor.id = :doctorId
      order by a.startsAt asc, a.id asc
      """)
  List<UUID> findPageIdsByDoctorId(@Param("doctorId") UUID doctorId, Pageable pageable);
}
