package com.ezderm.appointment.service.implementation;

import com.ezderm.appointment.api.model.Appointment;
import com.ezderm.appointment.api.model.AppointmentPageResponse;
import com.ezderm.appointment.api.model.CreateAppointmentRequest;
import com.ezderm.appointment.api.model.PageInfo;
import com.ezderm.appointment.repository.entity.AppointmentEntity;
import com.ezderm.appointment.repository.entity.AppointmentStatus;
import com.ezderm.appointment.repository.entity.DoctorEntity;
import com.ezderm.appointment.repository.entity.PatientEntity;
import com.ezderm.appointment.repository.jpa.AppointmentRepository;
import com.ezderm.appointment.repository.jpa.DoctorRepository;
import com.ezderm.appointment.repository.jpa.PatientRepository;
import com.ezderm.appointment.service.AppointmentService;
import com.ezderm.appointment.service.exception.ForbiddenException;
import com.ezderm.appointment.service.exception.MissingUsernameException;
import com.ezderm.appointment.service.exception.NotFoundException;
import com.ezderm.appointment.service.exception.ValidationException;
import com.ezderm.appointment.service.mapper.AppointmentMapper;
import com.ezderm.appointment.service.security.CurrentUser;
import com.ezderm.appointment.service.util.PageUtil;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class AppointmentServiceImpl implements AppointmentService {

  private final AppointmentRepository appointmentRepository;
  private final PatientRepository patientRepository;
  private final DoctorRepository doctorRepository;
  private final AppointmentMapper appointmentMapper;
  private final CurrentUser currentUser;

  @Override
  @Transactional
  public Appointment createAppointment(CreateAppointmentRequest request) {
    validateAppointmentTimes(request);

    PatientEntity patient =
        patientRepository
            .findByIdAndDeletedAtIsNull(request.getPatientId())
            .orElseThrow(() -> new NotFoundException("Patient not found"));
    DoctorEntity creator =
        doctorRepository
            .findByIdAndDeletedAtIsNull(request.getCreatedByDoctorId())
            .orElseThrow(() -> new NotFoundException("Current doctor not found"));
    List<DoctorEntity> doctors = activeDoctorsInRequestOrder(request.getDoctorIds());

    AppointmentEntity appointment =
        new AppointmentEntity(
            patient, creator, request.getStartsAt().toInstant(), request.getEndsAt().toInstant());
    for (int i = 0; i < doctors.size(); i++) {
      appointment.addDoctorParticipant(doctors.get(i), i);
    }

    return appointmentMapper.mapToDto(appointmentRepository.save(appointment));
  }

  @Override
  @Transactional(readOnly = true)
  public Appointment getAppointment(UUID id) {
    return appointmentMapper.mapToDto(findAppointment(id));
  }

  @Override
  @Transactional
  public Appointment cancelAppointment(UUID id) {
    AppointmentEntity appointment = findAppointment(id);
    if (!appointment.getCreatedByDoctor().getUsername().equals(requiredUsername())) {
      throw new ForbiddenException("Only the creating doctor can cancel this appointment");
    }

    if (appointment.getStatus() == AppointmentStatus.ACTIVE) {
      appointment.setStatus(AppointmentStatus.CANCELED);
      appointment.setCanceledAt(Instant.now());
    }

    return appointmentMapper.mapToDto(appointment);
  }

  @Override
  @Transactional(readOnly = true)
  public AppointmentPageResponse getAppointmentsByDoctor(
      UUID doctorId, Integer page, Integer size) {
    doctorRepository
        .findByIdAndDeletedAtIsNull(doctorId)
        .orElseThrow(() -> new NotFoundException("Doctor not found"));

    int pageNumber = PageUtil.requireValidPageOrDefault(page);
    int pageSize = PageUtil.requireValidSizeOrDefault(size);
    List<UUID> pageIds =
        new ArrayList<>(
            appointmentRepository.findPageIdsByDoctorId(
                doctorId, PageRequest.of(pageNumber, pageSize)));

    Map<UUID, AppointmentEntity> appointmentsById =
        pageIds.stream()
            .map(this::findAppointment)
            .collect(Collectors.toMap(AppointmentEntity::getId, Function.identity()));
    List<Appointment> appointments =
        pageIds.stream().map(appointmentsById::get).map(appointmentMapper::mapToDto).toList();

    return new AppointmentPageResponse()
        .items(appointments)
        .page(new PageInfo(pageNumber, pageSize));
  }

  private AppointmentEntity findAppointment(UUID id) {
    return appointmentRepository
        .findByIdWithDetails(id)
        .orElseThrow(() -> new NotFoundException("Appointment not found"));
  }

  private List<DoctorEntity> activeDoctorsInRequestOrder(List<UUID> doctorIds) {
    if (doctorIds == null || doctorIds.isEmpty()) {
      throw new ValidationException("At least one doctor is required");
    }

    List<UUID> distinctIds = new ArrayList<>(new LinkedHashSet<>(doctorIds));
    List<DoctorEntity> doctors = doctorRepository.findByIdInAndDeletedAtIsNull(distinctIds);
    if (doctors.size() != distinctIds.size()) {
      throw new NotFoundException("One or more doctors were not found");
    }

    Map<UUID, DoctorEntity> doctorsById =
        doctors.stream().collect(Collectors.toMap(DoctorEntity::getId, Function.identity()));
    return distinctIds.stream().map(doctorsById::get).toList();
  }

  private String requiredUsername() {
    String username = currentUser.username();
    if (username == null || username.isBlank()) {
      throw new MissingUsernameException("Username header missing");
    }
    return username;
  }

  private void validateAppointmentTimes(CreateAppointmentRequest request) {
    if (request.getStartsAt() == null || request.getEndsAt() == null) {
      throw new ValidationException("Appointment start and end are required");
    }
    if (!request.getEndsAt().isAfter(request.getStartsAt())) {
      throw new ValidationException("Appointment end must be after start");
    }
  }
}
