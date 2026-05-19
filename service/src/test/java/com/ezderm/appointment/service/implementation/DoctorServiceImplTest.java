package com.ezderm.appointment.service.implementation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ezderm.appointment.api.model.CreateDoctorRequest;
import com.ezderm.appointment.repository.entity.AppointmentStatus;
import com.ezderm.appointment.repository.entity.DoctorEntity;
import com.ezderm.appointment.repository.jpa.AppointmentRepository;
import com.ezderm.appointment.repository.jpa.DoctorRepository;
import com.ezderm.appointment.service.exception.ValidationException;
import com.ezderm.appointment.service.mapper.DoctorMapper;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class DoctorServiceImplTest {

  @Mock private DoctorRepository doctorRepository;
  @Mock private AppointmentRepository appointmentRepository;
  @Mock private DoctorMapper doctorMapper;

  @InjectMocks private DoctorServiceImpl doctorService;

  @Test
  void createDoctorRejectsExistingUsername() {
    CreateDoctorRequest request = new CreateDoctorRequest("drsmith", "John", "Smith");

    when(doctorRepository.existsByUsername("drsmith")).thenReturn(true);

    ValidationException exception =
        assertThrows(ValidationException.class, () -> doctorService.createDoctor(request));

    assertEquals("Doctor username already exists", exception.getMessage());
    verify(doctorRepository).existsByUsername("drsmith");
    verify(doctorRepository, never()).saveAndFlush(any());
    verifyNoInteractions(doctorMapper);
  }

  @Test
  void createDoctorTranslatesDuplicateConstraintViolation() {
    CreateDoctorRequest request = new CreateDoctorRequest("drsmith", "John", "Smith");
    DoctorEntity mappedEntity = new DoctorEntity("drsmith", "John", "Smith");

    when(doctorRepository.existsByUsername("drsmith")).thenReturn(false);
    when(doctorMapper.mapCreateRequestToEntity(request)).thenReturn(mappedEntity);
    when(doctorRepository.saveAndFlush(mappedEntity))
        .thenThrow(new DataIntegrityViolationException("duplicate"));

    ValidationException exception =
        assertThrows(ValidationException.class, () -> doctorService.createDoctor(request));

    assertEquals("Doctor username already exists", exception.getMessage());
    verify(doctorMapper).mapCreateRequestToEntity(request);
    verify(doctorRepository).saveAndFlush(mappedEntity);
  }

  @Test
  void deleteDoctorMarksDoctorDeletedWhenNoFutureAppointmentsExist() {
    UUID doctorId = UUID.randomUUID();
    DoctorEntity doctor = new DoctorEntity("drsmith", "John", "Smith");

    when(doctorRepository.findByIdAndDeletedAtIsNull(doctorId)).thenReturn(Optional.of(doctor));
    when(appointmentRepository.existsByDoctorParticipants_Doctor_IdAndStatusAndStartsAtAfter(
            eq(doctorId), eq(AppointmentStatus.ACTIVE), any(Instant.class)))
        .thenReturn(false);

    doctorService.deleteDoctor(doctorId);

    assertNotNull(doctor.getDeletedAt());
  }
}
