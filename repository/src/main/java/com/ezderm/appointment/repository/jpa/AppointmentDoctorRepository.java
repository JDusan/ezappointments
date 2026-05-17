package com.ezderm.appointment.repository.jpa;

import com.ezderm.appointment.repository.entity.AppointmentDoctorEntity;
import com.ezderm.appointment.repository.entity.AppointmentDoctorId;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentDoctorRepository
    extends JpaRepository<AppointmentDoctorEntity, AppointmentDoctorId> {

  List<AppointmentDoctorEntity> findById_AppointmentIdOrderBySortOrder(UUID appointmentId);
}
