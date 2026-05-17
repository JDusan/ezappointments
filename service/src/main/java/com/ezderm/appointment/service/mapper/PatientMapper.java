package com.ezderm.appointment.service.mapper;

import com.ezderm.appointment.api.model.CreatePatientRequest;
import com.ezderm.appointment.api.model.Patient;
import com.ezderm.appointment.repository.entity.PatientEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PatientMapper {

  Patient mapToDto(PatientEntity entity);

  default PatientEntity mapToEntity(CreatePatientRequest request) {
    return new PatientEntity(
        request.getFirstName(),
        request.getMiddleName(),
        request.getLastName(),
        request.getDateOfBirth());
  }
}
