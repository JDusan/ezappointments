package com.ezderm.appointment.service.mapper;

import com.ezderm.appointment.api.model.Appointment;
import com.ezderm.appointment.api.model.Doctor;
import com.ezderm.appointment.repository.entity.AppointmentDoctorEntity;
import com.ezderm.appointment.repository.entity.AppointmentEntity;
import com.ezderm.appointment.repository.entity.AppointmentStatus;
import com.ezderm.appointment.repository.entity.DoctorEntity;
import java.util.Comparator;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(
    componentModel = "spring",
    uses = {DoctorMapper.class, PatientMapper.class, TimeMapper.class})
public interface AppointmentMapper {

  @Mapping(source = "createdByDoctor.id", target = "createdByDoctorId")
  @Mapping(
      source = "doctorParticipants",
      target = "doctors",
      qualifiedByName = "mapDoctorParticipantsToDto")
  Appointment mapToDto(AppointmentEntity entity);

  default Appointment.StatusEnum mapToStatusDto(AppointmentStatus status) {
    return status == null ? null : Appointment.StatusEnum.fromValue(status.name());
  }

  @Named("mapDoctorParticipantsToDto")
  default List<Doctor> mapDoctorParticipantsToDto(List<AppointmentDoctorEntity> participants) {
    if (participants == null) {
      return List.of();
    }
    return participants.stream()
        .sorted(Comparator.comparingInt(AppointmentDoctorEntity::getSortOrder))
        .map(AppointmentDoctorEntity::getDoctor)
        .map(this::mapToDoctorDto)
        .toList();
  }

  Doctor mapToDoctorDto(DoctorEntity entity);
}
