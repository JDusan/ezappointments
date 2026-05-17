package com.ezderm.appointment.repository.jpa;

import com.ezderm.appointment.repository.entity.PatientEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.util.List;

public class PatientSearchRepositoryImpl implements PatientSearchRepository {

  private static final String SEARCH_EXPRESSION =
      "lower(first_name || ' ' || coalesce(middle_name, '') || ' ' || last_name)";

  @PersistenceContext private EntityManager entityManager;

  @Override
  public List<PatientEntity> searchActivePatients(String searchTerm, int page, int size) {
    String sql =
        """
        select *
        from patient
        where deleted_at is null
          and %s ilike :pattern
        order by last_name asc, id asc
        """
            .formatted(SEARCH_EXPRESSION);

    Query nativeQuery =
        entityManager
            .createNativeQuery(sql, PatientEntity.class)
            .setParameter("pattern", "%" + searchTerm + "%")
            .setFirstResult(Math.multiplyExact(page, size))
            .setMaxResults(size);

    @SuppressWarnings("unchecked")
    List<PatientEntity> patients = nativeQuery.getResultList();
    return patients;
  }
}
