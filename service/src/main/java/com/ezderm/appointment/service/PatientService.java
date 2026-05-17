package com.ezderm.appointment.service;

import com.ezderm.appointment.api.model.CreatePatientRequest;
import com.ezderm.appointment.api.model.Patient;
import com.ezderm.appointment.api.model.PatientPageResponse;
import com.ezderm.appointment.api.model.SearchPatientRequest;
import java.util.UUID;

public interface PatientService {

  Patient createPatient(CreatePatientRequest request);

  PatientPageResponse searchPatients(SearchPatientRequest request, Integer page, Integer size);

  void deletePatient(UUID id);
}
