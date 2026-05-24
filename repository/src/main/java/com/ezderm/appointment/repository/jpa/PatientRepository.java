package com.ezderm.appointment.repository.jpa;

import com.ezderm.appointment.repository.entity.PatientEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientRepository
    extends JpaRepository<PatientEntity, UUID>, PatientSearchRepository {

  Optional<PatientEntity> findByIdAndDeletedAtIsNull(UUID id);
}
