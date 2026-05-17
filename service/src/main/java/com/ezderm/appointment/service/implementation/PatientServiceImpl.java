package com.ezderm.appointment.service.implementation;

import com.ezderm.appointment.api.model.*;
import com.ezderm.appointment.repository.entity.AppointmentStatus;
import com.ezderm.appointment.repository.entity.PatientEntity;
import com.ezderm.appointment.repository.jpa.AppointmentRepository;
import com.ezderm.appointment.repository.jpa.PatientRepository;
import com.ezderm.appointment.service.PatientService;
import com.ezderm.appointment.service.exception.ConflictException;
import com.ezderm.appointment.service.exception.NotFoundException;
import com.ezderm.appointment.service.exception.ValidationException;
import com.ezderm.appointment.service.mapper.PatientMapper;
import com.ezderm.appointment.service.util.PageUtil;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class PatientServiceImpl implements PatientService {

  private final PatientRepository patientRepository;
  private final AppointmentRepository appointmentRepository;
  private final PatientMapper patientMapper;

  @Override
  @Transactional
  public Patient createPatient(CreatePatientRequest request) {
    PatientEntity patientEntity = patientMapper.mapToEntity(request);
    PatientEntity savedPatientEntity = patientRepository.save(patientEntity);
    return patientMapper.mapToDto(savedPatientEntity);
  }

  @Override
  @Transactional(readOnly = true)
  public PatientPageResponse searchPatients(
      SearchPatientRequest request, Integer page, Integer size) {
    String query = request.getQuery() == null ? "" : request.getQuery().trim();
    if (query.isEmpty()) {
      throw new ValidationException("Patient search query is required");
    }

    int pageNumber = PageUtil.requireValidPageOrDefault(page);
    int pageSize = PageUtil.requireValidSizeOrDefault(size);
    List<PatientEntity> entities =
        patientRepository.searchActivePatients(query, pageNumber, pageSize);

    return new PatientPageResponse()
        .items(entities.stream().map(patientMapper::mapToDto).toList())
        .page(new PageInfo(pageNumber, pageSize));
  }

  @Override
  @Transactional
  public void deletePatient(UUID id) {
    PatientEntity patient =
        patientRepository
            .findByIdAndDeletedAtIsNull(id)
            .orElseThrow(() -> new NotFoundException("Patient not found"));

    if (appointmentRepository.existsByPatient_IdAndStatusAndStartsAtAfter(
        id, AppointmentStatus.ACTIVE, Instant.now())) {
      throw new ConflictException("Patient has active future appointments");
    }

    patient.setDeletedAt(Instant.now());
  }
}
