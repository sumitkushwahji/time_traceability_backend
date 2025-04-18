package com.time.tracealibility.repository;

import com.time.tracealibility.entity.SatCommonViewDifference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public interface SatCommonViewDifferenceRepository extends JpaRepository<SatCommonViewDifference, String> {

    @Query(
            value = "SELECT * FROM sat_common_view_difference s WHERE " +
                    "CAST(s.sat_letter AS TEXT) ILIKE %:search% OR " +
                    "CAST(s.mjd AS TEXT) ILIKE %:search% OR " +
                    "CAST(s.common_sattelite AS TEXT) ILIKE %:search% OR " +
                    "CAST(s.sttime AS TEXT) ILIKE %:search% OR " +
                    "CAST(s.mjd_date_time AS TEXT) ILIKE %:search% OR " +
                    "CAST(s.source1 AS TEXT) ILIKE %:search% OR " +
                    "CAST(s.source2 AS TEXT) ILIKE %:search% OR " +
                    "CAST(s.avg1 AS TEXT) ILIKE %:search% OR " +
                    "CAST(s.avg2 AS TEXT) ILIKE %:search% OR " +
                    "CAST(s.avg_refsys_difference AS TEXT) ILIKE %:search%",
            countQuery = "SELECT count(*) FROM sat_common_view_difference s WHERE " +
                    "CAST(s.sat_letter AS TEXT) ILIKE %:search% OR " +
                    "CAST(s.mjd AS TEXT) ILIKE %:search% OR " +
                    "CAST(s.common_sattelite AS TEXT) ILIKE %:search% OR " +
                    "CAST(s.sttime AS TEXT) ILIKE %:search% OR " +
                    "CAST(s.mjd_date_time AS TEXT) ILIKE %:search% OR " +
                    "CAST(s.source1 AS TEXT) ILIKE %:search% OR " +
                    "CAST(s.source2 AS TEXT) ILIKE %:search% OR " +
                    "CAST(s.avg1 AS TEXT) ILIKE %:search% OR " +
                    "CAST(s.avg2 AS TEXT) ILIKE %:search% OR " +
                    "CAST(s.avg_refsys_difference AS TEXT) ILIKE %:search%",
            nativeQuery = true
    )
    Page<SatCommonViewDifference> searchAll(@Param("search") String search, Pageable pageable);

    // Custom query to find records within a date range
    @Query("SELECT s FROM SatCommonViewDifference s WHERE s.mjdDateTime BETWEEN :startDate AND :endDate")
    Page<SatCommonViewDifference> findByDateRange(
            @Param("startDate") OffsetDateTime startDate,
            @Param("endDate") OffsetDateTime endDate,
            Pageable pageable
    );

    @Query(
            value = "SELECT * FROM sat_common_view_difference s WHERE " +
                    "(LOWER(s.sat_letter) LIKE %:search% OR LOWER(s.sttime) LIKE %:search% OR " +
                    "CAST(s.mjd AS TEXT) LIKE %:search% OR LOWER(s.common_sattelite) LIKE %:search%) " +
                    "AND s.mjd_date_time BETWEEN :startDate AND :endDate",
            countQuery = "SELECT count(*) FROM sat_common_view_difference s WHERE " +
                    "(LOWER(s.sat_letter) LIKE %:search% OR LOWER(s.sttime) LIKE %:search% OR " +
                    "CAST(s.mjd AS TEXT) LIKE %:search% OR LOWER(s.common_sattelite) LIKE %:search%) " +
                    "AND s.mjd_date_time BETWEEN :startDate AND :endDate",
            nativeQuery = true
    )
    Page<SatCommonViewDifference> searchAllWithDateRange(
            @Param("search") String search,
            @Param("startDate") OffsetDateTime startDate,
            @Param("endDate") OffsetDateTime endDate,
            Pageable pageable
    );



}
