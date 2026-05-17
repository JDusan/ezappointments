package com.ezderm.appointment.service.implementation;

import com.ezderm.appointment.api.model.CreateDoctorRequest;
import com.ezderm.appointment.api.model.Doctor;
import com.ezderm.appointment.repository.entity.AppointmentStatus;
import com.ezderm.appointment.repository.entity.DoctorEntity;
import com.ezderm.appointment.repository.jpa.AppointmentRepository;
import com.ezderm.appointment.repository.jpa.DoctorRepository;
import com.ezderm.appointment.service.DoctorService;
import com.ezderm.appointment.service.exception.ConflictException;
import com.ezderm.appointment.service.exception.NotFoundException;
import com.ezderm.appointment.service.exception.ValidationException;
import com.ezderm.appointment.service.mapper.DoctorMapper;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class DoctorServiceImpl implements DoctorService {

  private final DoctorRepository doctorRepository;
  private final AppointmentRepository appointmentRepository;
  private final DoctorMapper doctorMapper;

  @Override
  @Transactional
  public Doctor createDoctor(CreateDoctorRequest request) {
    if (doctorRepository.existsByUsername(request.getUsername())) {
      throw new ValidationException("Doctor username already exists");
    }

    try {
      DoctorEntity doctorEntity = doctorMapper.mapCreateRequestToEntity(request);
      DoctorEntity savedDoctorEntity = doctorRepository.saveAndFlush(doctorEntity);
      return doctorMapper.mapToDto(savedDoctorEntity);
    } catch (DataIntegrityViolationException ex) {
      throw new ValidationException("Doctor username already exists");
    }
  }

  @Override
  @Transactional
  public void deleteDoctor(UUID id) {
    DoctorEntity doctor =
        doctorRepository
            .findByIdAndDeletedAtIsNull(id)
            .orElseThrow(() -> new NotFoundException("Doctor not found"));

    if (appointmentRepository.existsByDoctorParticipants_Doctor_IdAndStatusAndStartsAtAfter(
        id, AppointmentStatus.ACTIVE, Instant.now())) {
      throw new ConflictException("Doctor has active future appointments");
    }

    doctor.setDeletedAt(Instant.now());
  }
}
