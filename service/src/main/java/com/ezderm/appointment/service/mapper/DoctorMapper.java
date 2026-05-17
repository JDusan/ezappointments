package com.ezderm.appointment.service.mapper;

import com.ezderm.appointment.api.model.CreateDoctorRequest;
import com.ezderm.appointment.api.model.Doctor;
import com.ezderm.appointment.repository.entity.DoctorEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DoctorMapper {

  Doctor mapToDto(DoctorEntity entity);

  default DoctorEntity mapCreateRequestToEntity(CreateDoctorRequest request) {
    return new DoctorEntity(request.getUsername(), request.getFirstName(), request.getLastName());
  }
}
