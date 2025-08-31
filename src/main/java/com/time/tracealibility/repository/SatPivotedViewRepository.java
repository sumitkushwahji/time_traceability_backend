package com.time.tracealibility.repository;

import com.time.tracealibility.entity.SatPivotedView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SatPivotedViewRepository extends JpaRepository<SatPivotedView, String> {

  @Query(
    value = "SELECT * FROM sat_pivoted_view_materialized s " +
      "WHERE (:startDate IS NULL OR s.mjd_date_time >= CAST(:startDate AS TIMESTAMP)) " +
      "AND (:endDate IS NULL OR s.mjd_date_time <= CAST(:endDate AS TIMESTAMP)) " +
      "AND (:satLetter IS NULL OR LOWER(s.sat_letter) = LOWER(:satLetter))",
    countQuery = "SELECT count(*) FROM sat_pivoted_view_materialized s " +
      "WHERE (:startDate IS NULL OR s.mjd_date_time >= CAST(:startDate AS TIMESTAMP)) " +
      "AND (:endDate IS NULL OR s.mjd_date_time <= CAST(:endDate AS TIMESTAMP)) " +
      "AND (:satLetter IS NULL OR LOWER(s.sat_letter) = LOWER(:satLetter))",
    nativeQuery = true
  )
  Page<SatPivotedView> findByFilters(
    @Param("startDate") String startDate,
    @Param("endDate") String endDate,
    @Param("satLetter") String satLetter,
    Pageable pageable
  );
}
