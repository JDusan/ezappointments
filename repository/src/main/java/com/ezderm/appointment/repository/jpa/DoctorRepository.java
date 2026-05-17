package com.ezderm.appointment.repository.jpa;

import com.ezderm.appointment.repository.entity.DoctorEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DoctorRepository extends JpaRepository<DoctorEntity, UUID> {

  Optional<DoctorEntity> findByIdAndDeletedAtIsNull(UUID id);

  Optional<DoctorEntity> findByUsernameAndDeletedAtIsNull(String username);

  List<DoctorEntity> findByIdInAndDeletedAtIsNull(List<UUID> ids);

  boolean existsByUsername(String username);
}
