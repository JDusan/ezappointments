package com.ezderm.appointment.repository.jpa;

import com.ezderm.appointment.repository.entity.PatientEntity;
import java.util.List;

public interface PatientSearchRepository {

  List<PatientEntity> searchActivePatients(String searchTerm, int page, int size);
}
