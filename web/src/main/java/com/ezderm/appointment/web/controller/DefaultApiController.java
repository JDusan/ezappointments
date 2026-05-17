package com.ezderm.appointment.web.controller;

import com.ezderm.appointment.api.controller.DefaultApi;
import com.ezderm.appointment.api.model.Appointment;
import com.ezderm.appointment.api.model.AppointmentPageResponse;
import com.ezderm.appointment.api.model.CreateAppointmentRequest;
import com.ezderm.appointment.api.model.CreateDoctorRequest;
import com.ezderm.appointment.api.model.CreatePatientRequest;
import com.ezderm.appointment.api.model.Doctor;
import com.ezderm.appointment.api.model.Patient;
import com.ezderm.appointment.api.model.PatientPageResponse;
import com.ezderm.appointment.api.model.SearchPatientRequest;
import com.ezderm.appointment.service.AppointmentService;
import com.ezderm.appointment.service.DoctorService;
import com.ezderm.appointment.service.PatientService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
class DefaultApiController implements DefaultApi {

  private final AppointmentService appointmentService;
  private final PatientService patientService;
  private final DoctorService doctorService;

  @Override
  public ResponseEntity<Appointment> cancelAppointment(String xUsername, UUID id) {
    return ResponseEntity.ok(appointmentService.cancelAppointment(id));
  }

  @Override
  public ResponseEntity<Appointment> createAppointment(
      String xUsername, CreateAppointmentRequest createAppointmentRequest) {
    Appointment appointment = appointmentService.createAppointment(createAppointmentRequest);
    return ResponseEntity.status(HttpStatus.CREATED).body(appointment);
  }

  @Override
  public ResponseEntity<Doctor> createDoctor(
      String xUsername, CreateDoctorRequest createDoctorRequest) {
    Doctor doctor = doctorService.createDoctor(createDoctorRequest);
    return ResponseEntity.status(HttpStatus.CREATED).body(doctor);
  }

  @Override
  public ResponseEntity<Patient> createPatient(
      String xUsername, CreatePatientRequest createPatientRequest) {
    Patient patient = patientService.createPatient(createPatientRequest);
    return ResponseEntity.status(HttpStatus.CREATED).body(patient);
  }

  @Override
  public ResponseEntity<Void> deleteDoctor(String xUsername, UUID id) {
    doctorService.deleteDoctor(id);
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<Void> deletePatient(String xUsername, UUID id) {
    patientService.deletePatient(id);
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<Appointment> getAppointment(String xUsername, UUID id) {
    return ResponseEntity.ok(appointmentService.getAppointment(id));
  }

  @Override
  public ResponseEntity<AppointmentPageResponse> getAppointmentsByDoctor(
      String xUsername, UUID id, Integer page, Integer size) {
    return ResponseEntity.ok(appointmentService.getAppointmentsByDoctor(id, page, size));
  }

  @Override
  public ResponseEntity<PatientPageResponse> searchPatients(
      String xUsername, SearchPatientRequest searchPatientRequest, Integer page, Integer size) {
    return ResponseEntity.ok(patientService.searchPatients(searchPatientRequest, page, size));
  }
}
