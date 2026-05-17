package com.ezderm.appointment.service;

import com.ezderm.appointment.api.model.Appointment;
import com.ezderm.appointment.api.model.AppointmentPageResponse;
import com.ezderm.appointment.api.model.CreateAppointmentRequest;
import java.util.UUID;

public interface AppointmentService {

  Appointment createAppointment(CreateAppointmentRequest request);

  Appointment getAppointment(UUID id);

  Appointment cancelAppointment(UUID id);

  AppointmentPageResponse getAppointmentsByDoctor(UUID doctorId, Integer page, Integer size);
}
