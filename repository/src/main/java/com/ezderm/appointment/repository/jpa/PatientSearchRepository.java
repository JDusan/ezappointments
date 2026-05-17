package com.ezderm.appointment.repository.jpa;

import com.ezderm.appointment.repository.entity.PatientEntity;
import java.util.List;
import java.util.UUID;

public interface PatientSearchRepository {

  List<PatientEntity> searchActivePatients(
      String searchTerm, String cursorLastName, UUID cursorId, int limit);
}
