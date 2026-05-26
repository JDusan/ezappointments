package com.ezderm.appointment.service.implementation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.ezderm.appointment.api.model.Appointment;
import com.ezderm.appointment.api.model.AppointmentPageResponse;
import com.ezderm.appointment.api.model.CreateAppointmentRequest;
import com.ezderm.appointment.repository.entity.AppointmentEntity;
import com.ezderm.appointment.repository.entity.AppointmentStatus;
import com.ezderm.appointment.repository.entity.DoctorEntity;
import com.ezderm.appointment.repository.entity.PatientEntity;
import com.ezderm.appointment.repository.jpa.AppointmentRepository;
import com.ezderm.appointment.repository.jpa.DoctorRepository;
import com.ezderm.appointment.repository.jpa.PatientRepository;
import com.ezderm.appointment.service.exception.ForbiddenException;
import com.ezderm.appointment.service.exception.ValidationException;
import com.ezderm.appointment.service.mapper.AppointmentMapper;
import com.ezderm.appointment.service.security.CurrentUser;
import java.lang.reflect.Field;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceImplTest {

  @Mock private AppointmentRepository appointmentRepository;
  @Mock private PatientRepository patientRepository;
  @Mock private DoctorRepository doctorRepository;
  @Mock private AppointmentMapper appointmentMapper;
  @Mock private CurrentUser currentUser;

  @InjectMocks private AppointmentServiceImpl appointmentService;

  @Test
  void createAppointmentRejectsWhenEndIsNotAfterStart() {
    OffsetDateTime startsAt = OffsetDateTime.of(2026, 5, 20, 9, 0, 0, 0, ZoneOffset.UTC);
    CreateAppointmentRequest request =
        new CreateAppointmentRequest()
            .patientId(UUID.randomUUID())
            .createdByDoctorId(UUID.randomUUID())
            .doctorIds(List.of(UUID.randomUUID()))
            .startsAt(startsAt)
            .endsAt(startsAt.minusMinutes(1));

    ValidationException exception =
        assertThrows(
            ValidationException.class, () -> appointmentService.createAppointment(request));

    assertEquals("Appointment end must be after start", exception.getMessage());
    verifyNoInteractions(
        patientRepository, doctorRepository, appointmentRepository, appointmentMapper);
  }

  @Test
  void createAppointmentSavesDistinctDoctorsInRequestOrder() {
    UUID patientId = UUID.randomUUID();
    UUID creatorId = UUID.randomUUID();
    UUID doctorIdOne = UUID.randomUUID();
    UUID doctorIdTwo = UUID.randomUUID();
    OffsetDateTime startsAt = OffsetDateTime.of(2026, 5, 20, 9, 0, 0, 0, ZoneOffset.UTC);
    OffsetDateTime endsAt = startsAt.plusMinutes(30);
    CreateAppointmentRequest request =
        new CreateAppointmentRequest()
            .patientId(patientId)
            .createdByDoctorId(creatorId)
            .doctorIds(List.of(doctorIdTwo, doctorIdOne, doctorIdTwo))
            .startsAt(startsAt)
            .endsAt(endsAt);

    PatientEntity patient = new PatientEntity("Jane", null, "Doe", LocalDate.of(1990, 2, 3));
    DoctorEntity creator = new DoctorEntity("creator", "Chris", "Creator");
    DoctorEntity doctorOne = new DoctorEntity("doctor1", "Alex", "One");
    DoctorEntity doctorTwo = new DoctorEntity("doctor2", "Blair", "Two");
    Appointment dto = new Appointment().id(UUID.randomUUID());
    setEntityId(doctorOne, doctorIdOne);
    setEntityId(doctorTwo, doctorIdTwo);

    when(patientRepository.findByIdAndDeletedAtIsNull(patientId)).thenReturn(Optional.of(patient));
    when(doctorRepository.findByIdAndDeletedAtIsNull(creatorId)).thenReturn(Optional.of(creator));
    when(doctorRepository.findByIdInAndDeletedAtIsNull(List.of(doctorIdTwo, doctorIdOne)))
        .thenReturn(List.of(doctorOne, doctorTwo));
    when(appointmentRepository.save(any(AppointmentEntity.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(appointmentMapper.mapToDto(any(AppointmentEntity.class))).thenReturn(dto);

    Appointment result = appointmentService.createAppointment(request);

    ArgumentCaptor<AppointmentEntity> appointmentCaptor =
        ArgumentCaptor.forClass(AppointmentEntity.class);
    verify(appointmentRepository).save(appointmentCaptor.capture());
    AppointmentEntity savedAppointment = appointmentCaptor.getValue();

    assertSame(dto, result);
    assertSame(patient, savedAppointment.getPatient());
    assertSame(creator, savedAppointment.getCreatedByDoctor());
    assertEquals(startsAt.toInstant(), savedAppointment.getStartsAt());
    assertEquals(endsAt.toInstant(), savedAppointment.getEndsAt());
    assertEquals(2, savedAppointment.getDoctorParticipants().size());
    assertSame(doctorTwo, savedAppointment.getDoctorParticipants().get(0).getDoctor());
    assertEquals(0, savedAppointment.getDoctorParticipants().get(0).getSortOrder());
    assertSame(doctorOne, savedAppointment.getDoctorParticipants().get(1).getDoctor());
    assertEquals(1, savedAppointment.getDoctorParticipants().get(1).getSortOrder());
  }

  @Test
  void cancelAppointmentRejectsNonCreatorDoctor() {
    UUID appointmentId = UUID.randomUUID();
    DoctorEntity creator = new DoctorEntity("creator", "Chris", "Creator");
    AppointmentEntity appointment =
        new AppointmentEntity(
            new PatientEntity("Jane", null, "Doe", LocalDate.of(1990, 2, 3)),
            creator,
            Instant.parse("2026-05-20T09:00:00Z"),
            Instant.parse("2026-05-20T09:30:00Z"));

    when(appointmentRepository.findByIdWithDetails(appointmentId))
        .thenReturn(Optional.of(appointment));
    when(currentUser.username()).thenReturn("other-doctor");

    ForbiddenException exception =
        assertThrows(
            ForbiddenException.class, () -> appointmentService.cancelAppointment(appointmentId));

    assertEquals("Only the creating doctor can cancel this appointment", exception.getMessage());
    assertEquals(AppointmentStatus.ACTIVE, appointment.getStatus());
    assertNull(appointment.getCanceledAt());
    verify(appointmentMapper, never()).mapToDto(any());
  }

  @Test
  void getAppointmentsByDoctorReturnsMappedAppointmentsInRepositoryPageOrder() {
    UUID doctorId = UUID.randomUUID();
    UUID appointmentIdOne = UUID.randomUUID();
    UUID appointmentIdTwo = UUID.randomUUID();
    DoctorEntity doctor = new DoctorEntity("drsmith", "John", "Smith");
    AppointmentEntity appointmentOne =
        new AppointmentEntity(
            new PatientEntity("Jane", null, "Doe", LocalDate.of(1990, 2, 3)),
            doctor,
            Instant.parse("2026-05-20T09:00:00Z"),
            Instant.parse("2026-05-20T09:30:00Z"));
    AppointmentEntity appointmentTwo =
        new AppointmentEntity(
            new PatientEntity("Jack", null, "Doe", LocalDate.of(1991, 4, 5)),
            doctor,
            Instant.parse("2026-05-20T10:00:00Z"),
            Instant.parse("2026-05-20T10:30:00Z"));
    Appointment dtoOne = new Appointment().id(appointmentIdOne);
    Appointment dtoTwo = new Appointment().id(appointmentIdTwo);
    setEntityId(appointmentOne, appointmentIdOne);
    setEntityId(appointmentTwo, appointmentIdTwo);

    when(doctorRepository.findByIdAndDeletedAtIsNull(doctorId)).thenReturn(Optional.of(doctor));
    when(appointmentRepository.findPageIdsByDoctorId(eq(doctorId), any(Pageable.class)))
        .thenReturn(List.of(appointmentIdTwo, appointmentIdOne));
    when(appointmentRepository.findAllByIdWithDetails(List.of(appointmentIdTwo, appointmentIdOne)))
        .thenReturn(List.of(appointmentOne, appointmentTwo));
    when(appointmentMapper.mapToDto(appointmentOne)).thenReturn(dtoOne);
    when(appointmentMapper.mapToDto(appointmentTwo)).thenReturn(dtoTwo);

    AppointmentPageResponse result = appointmentService.getAppointmentsByDoctor(doctorId, 1, 5);

    assertEquals(List.of(dtoTwo, dtoOne), result.getItems());
    assertEquals(1, result.getPage().getPage());
    assertEquals(5, result.getPage().getSize());
    verify(appointmentRepository)
        .findPageIdsByDoctorId(
            eq(doctorId),
            argThat(pageable -> pageable.getPageNumber() == 1 && pageable.getPageSize() == 5));
  }

  private static void setEntityId(Object entity, UUID id) {
    try {
      Field idField = entity.getClass().getDeclaredField("id");
      idField.setAccessible(true);
      idField.set(entity, id);
    } catch (ReflectiveOperationException ex) {
      fail("Unable to set entity id for test setup", ex);
    }
  }
}
