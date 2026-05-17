package com.ezderm.appointment.service;

import com.ezderm.appointment.api.model.CreateDoctorRequest;
import com.ezderm.appointment.api.model.Doctor;
import java.util.UUID;

public interface DoctorService {

  Doctor createDoctor(CreateDoctorRequest request);

  void deleteDoctor(UUID id);
}
