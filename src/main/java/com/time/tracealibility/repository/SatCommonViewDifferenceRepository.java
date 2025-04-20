package com.time.tracealibility.repository;

import com.time.tracealibility.entity.SatCommonViewDifference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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






    @Query(
            value = "SELECT * FROM sat_common_view_difference s " +
                    "WHERE " +
                    "(" +
                    "   CAST(s.sat_letter AS TEXT) ILIKE %:search% OR " +
                    "   CAST(s.mjd AS TEXT) ILIKE %:search% OR " +
                    "   CAST(s.common_sattelite AS TEXT) ILIKE %:search% OR " +
                    "   CAST(s.sttime AS TEXT) ILIKE %:search% OR " +
                    "   CAST(s.mjd_date_time AS TEXT) ILIKE %:search% OR " +
                    "   CAST(s.source1 AS TEXT) ILIKE %:search% OR " +
                    "   CAST(s.source2 AS TEXT) ILIKE %:search% OR " +
                    "   CAST(s.avg1 AS TEXT) ILIKE %:search% OR " +
                    "   CAST(s.avg2 AS TEXT) ILIKE %:search% OR " +
                    "   CAST(s.avg_refsys_difference AS TEXT) ILIKE %:search%" +
                    ") " +
                    "AND " +
                    "(:startDate IS NULL OR s.mjd_date_time >= CAST(:startDate AS TIMESTAMP)) " +
                    "AND " +
                    "(:endDate IS NULL OR s.mjd_date_time <= CAST(:endDate AS TIMESTAMP))",
            countQuery = "SELECT count(*) FROM sat_common_view_difference s " +
                    "WHERE " +
                    "(" +
                    "   CAST(s.sat_letter AS TEXT) ILIKE %:search% OR " +
                    "   CAST(s.mjd AS TEXT) ILIKE %:search% OR " +
                    "   CAST(s.common_sattelite AS TEXT) ILIKE %:search% OR " +
                    "   CAST(s.sttime AS TEXT) ILIKE %:search% OR " +
                    "   CAST(s.mjd_date_time AS TEXT) ILIKE %:search% OR " +
                    "   CAST(s.source1 AS TEXT) ILIKE %:search% OR " +
                    "   CAST(s.source2 AS TEXT) ILIKE %:search% OR " +
                    "   CAST(s.avg1 AS TEXT) ILIKE %:search% OR " +
                    "   CAST(s.avg2 AS TEXT) ILIKE %:search% OR " +
                    "   CAST(s.avg_refsys_difference AS TEXT) ILIKE %:search%" +
                    ") " +
                    "AND " +
                    "(:startDate IS NULL OR s.mjd_date_time >= CAST(:startDate AS TIMESTAMP)) " +
                    "AND " +
                    "(:endDate IS NULL OR s.mjd_date_time <= CAST(:endDate AS TIMESTAMP))",
            nativeQuery = true
    )
    Page<SatCommonViewDifference> searchAllWithDateFilter(
            @Param("search") String search,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            Pageable pageable
    );


}

