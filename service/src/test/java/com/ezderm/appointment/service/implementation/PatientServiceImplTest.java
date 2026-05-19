package com.ezderm.appointment.service.implementation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ezderm.appointment.api.model.CreatePatientRequest;
import com.ezderm.appointment.api.model.Patient;
import com.ezderm.appointment.api.model.SearchPatientRequest;
import com.ezderm.appointment.repository.entity.AppointmentStatus;
import com.ezderm.appointment.repository.entity.PatientEntity;
import com.ezderm.appointment.repository.jpa.AppointmentRepository;
import com.ezderm.appointment.repository.jpa.PatientRepository;
import com.ezderm.appointment.service.exception.ConflictException;
import com.ezderm.appointment.service.exception.ValidationException;
import com.ezderm.appointment.service.mapper.PatientMapper;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PatientServiceImplTest {

  @Mock private PatientRepository patientRepository;
  @Mock private AppointmentRepository appointmentRepository;
  @Mock private PatientMapper patientMapper;

  @InjectMocks private PatientServiceImpl patientService;

  @Test
  void createPatientSavesMappedEntityAndReturnsMappedDto() {
    CreatePatientRequest request =
        new CreatePatientRequest("Jane", "Doe", LocalDate.of(1990, 2, 3));
    request.setMiddleName("Q");
    PatientEntity mappedEntity = new PatientEntity("Jane", "Q", "Doe", LocalDate.of(1990, 2, 3));
    PatientEntity savedEntity = new PatientEntity("Jane", "Q", "Doe", LocalDate.of(1990, 2, 3));
    Patient patientDto =
        new Patient().id(savedEntity.getId()).firstName("Jane").middleName("Q").lastName("Doe");

    when(patientMapper.mapToEntity(request)).thenReturn(mappedEntity);
    when(patientRepository.save(mappedEntity)).thenReturn(savedEntity);
    when(patientMapper.mapToDto(savedEntity)).thenReturn(patientDto);

    Patient result = patientService.createPatient(request);

    assertSame(patientDto, result);
    verify(patientMapper).mapToEntity(request);
    verify(patientRepository).save(mappedEntity);
    verify(patientMapper).mapToDto(savedEntity);
  }

  @Test
  void searchPatientsRejectsBlankQuery() {
    SearchPatientRequest request = new SearchPatientRequest("   ");

    ValidationException exception =
        assertThrows(
            ValidationException.class, () -> patientService.searchPatients(request, null, null));

    assertEquals("Patient search query is required", exception.getMessage());
    verifyNoInteractions(patientRepository, patientMapper);
  }

  @Test
  void deletePatientThrowsConflictWhenFutureAppointmentsExist() {
    UUID patientId = UUID.randomUUID();
    PatientEntity patient = new PatientEntity("Jane", null, "Doe", LocalDate.of(1990, 2, 3));

    when(patientRepository.findByIdAndDeletedAtIsNull(patientId)).thenReturn(Optional.of(patient));
    when(appointmentRepository.existsByPatient_IdAndStatusAndStartsAtAfter(
            eq(patientId), eq(AppointmentStatus.ACTIVE), any(Instant.class)))
        .thenReturn(true);

    ConflictException exception =
        assertThrows(ConflictException.class, () -> patientService.deletePatient(patientId));

    assertEquals("Patient has active future appointments", exception.getMessage());
    assertNull(patient.getDeletedAt());
  }
}
