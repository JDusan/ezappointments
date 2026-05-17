package com.ezderm.appointment.repository.jpa;

import com.ezderm.appointment.repository.entity.PatientEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.util.List;
import java.util.UUID;

public class PatientSearchRepositoryImpl implements PatientSearchRepository {

  private static final String SEARCH_EXPRESSION =
      "lower(first_name || ' ' || coalesce(middle_name, '') || ' ' || last_name)";

  @PersistenceContext private EntityManager entityManager;

  @Override
  public List<PatientEntity> searchActivePatients(
      String searchTerm, String cursorLastName, UUID cursorId, int limit) {
    if (cursorLastName != null && cursorId == null) {
      throw new IllegalArgumentException("cursorId is required when cursorLastName is present");
    }

    String sql =
        """
        select *
        from patient
        where deleted_at is null
          and %s ilike :pattern
          %s
        order by last_name asc, id asc
        limit least(coalesce(:limit, 20), 200)
        """
            .formatted(
                SEARCH_EXPRESSION,
                cursorLastName == null
                    ? ""
                    : "and (last_name > :cursorLastName "
                        + "or (last_name = :cursorLastName and id > cast(:cursorId as uuid)))");

    Query nativeQuery =
        entityManager
            .createNativeQuery(sql, PatientEntity.class)
            .setParameter("pattern", "%" + searchTerm + "%")
            .setParameter("limit", limit);

    if (cursorLastName != null) {
      nativeQuery.setParameter("cursorLastName", cursorLastName).setParameter("cursorId", cursorId);
    }

    @SuppressWarnings("unchecked")
    List<PatientEntity> patients = nativeQuery.getResultList();
    return patients;
  }
}
