package com.time.tracealibility.repository;

import com.time.tracealibility.entity.SatCombinedViewDifference;
import com.time.tracealibility.entity.SatCommonViewDifference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SatCommonViewDifferenceRepository extends JpaRepository<SatCommonViewDifference, String> {

    @Query(
            value = "SELECT * FROM sat_common_view_difference_materialized s WHERE " +
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
            countQuery = "SELECT count(*) FROM sat_common_view_difference_materialized s WHERE " +
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
            value = "SELECT * FROM sat_common_view_difference_materialized s " +
                    "WHERE (" +
                    "   :search IS NULL OR " +
                    "   CAST(s.sat_letter AS TEXT) ILIKE CONCAT('%', :search, '%') OR " +
                    "   CAST(s.mjd AS TEXT) ILIKE CONCAT('%', :search, '%') OR " +
                    "   CAST(s.common_sattelite AS TEXT) ILIKE CONCAT('%', :search, '%') OR " +
                    "   CAST(s.sttime AS TEXT) ILIKE CONCAT('%', :search, '%') OR " +
                    "   CAST(s.mjd_date_time AS TEXT) ILIKE CONCAT('%', :search, '%') OR " +
                    "   CAST(s.source1 AS TEXT) ILIKE CONCAT('%', :search, '%') OR " +
                    "   CAST(s.source2 AS TEXT) ILIKE CONCAT('%', :search, '%') OR " +
                    "   CAST(s.avg1 AS TEXT) ILIKE CONCAT('%', :search, '%') OR " +
                    "   CAST(s.avg2 AS TEXT) ILIKE CONCAT('%', :search, '%') OR " +
                    "   CAST(s.avg_refsys_difference AS TEXT) ILIKE CONCAT('%', :search, '%')" +
                    ") " +
                    "AND (:startDate IS NULL OR s.mjd_date_time >= CAST(:startDate AS TIMESTAMP)) " +
                    "AND (:endDate IS NULL OR s.mjd_date_time <= CAST(:endDate AS TIMESTAMP)) " +
                    "AND (:satLetter IS NULL OR LOWER(s.sat_letter) = LOWER(:satLetter)) " + // Changed to LOWER for robustness
                    "AND (:#{#source2 == null} OR s.source2 IN (:source2))", // Changed to use SpEL for List null check

            countQuery = "SELECT count(*) FROM sat_common_view_difference_materialized s " +
                    "WHERE (" +
                    "   :search IS NULL OR " +
                    "   CAST(s.sat_letter AS TEXT) ILIKE CONCAT('%', :search, '%') OR " +
                    "   CAST(s.mjd AS TEXT) ILIKE CONCAT('%', :search, '%') OR " +
                    "   CAST(s.common_sattelite AS TEXT) ILIKE CONCAT('%', :search, '%') OR " +
                    "   CAST(s.sttime AS TEXT) ILIKE CONCAT('%', :search, '%') OR " +
                    "   CAST(s.mjd_date_time AS TEXT) ILIKE CONCAT('%', :search, '%') OR " +
                    "   CAST(s.source1 AS TEXT) ILIKE CONCAT('%', :search, '%') OR " +
                    "   CAST(s.source2 AS TEXT) ILIKE CONCAT('%', :search, '%') OR " +
                    "   CAST(s.avg1 AS TEXT) ILIKE CONCAT('%', :search, '%') OR " +
                    "   CAST(s.avg2 AS TEXT) ILIKE CONCAT('%', :search, '%') OR " +
                    "   CAST(s.avg_refsys_difference AS TEXT) ILIKE CONCAT('%', :search, '%')" +
                    ") " +
                    "AND (:startDate IS NULL OR s.mjd_date_time >= CAST(:startDate AS TIMESTAMP)) " +
                    "AND (:endDate IS NULL OR s.mjd_date_time <= CAST(:endDate AS TIMESTAMP)) " +
                    "AND (:satLetter IS NULL OR LOWER(s.sat_letter) = LOWER(:satLetter)) " + // Changed to LOWER for robustness
                    "AND (:#{#source2 == null} OR s.source2 IN (:source2))", // Changed to use SpEL for List null check

            nativeQuery = true
    )
    Page<SatCommonViewDifference> searchAllWithDateFilter(
            @Param("search") String search,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            @Param("satLetter") String satLetter,
            @Param("source2") List<String> source2, // 🎯 Changed to List<String>
            Pageable pageable
    );

    // 🚀 NEW OPTIMIZED METHODS FOR PERFORMANCE

    /**
     * Fast bulk data query using materialized view for instant results
     */
    @Query(
            value = "SELECT * FROM sat_common_view_difference_materialized s " +
                    "WHERE (:#{#source2 == null} OR s.source2 IN (:source2)) " +
                    "AND (:startDate IS NULL OR s.mjd_date_time >= CAST(:startDate AS TIMESTAMP)) " +
                    "AND (:endDate IS NULL OR s.mjd_date_time <= CAST(:endDate AS TIMESTAMP))",
            nativeQuery = true
    )
    List<SatCommonViewDifference> findBulkByLocationAndDateRange(
            @Param("source2") List<String> source2,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate
    );

    /**
     * Optimized paginated query using materialized view with indexes
     */
    @Query(
            value = "SELECT * FROM sat_combined_view_difference_materialized s " +
                    "WHERE (:#{#source2 == null} OR s.source2 IN (:source2)) " +
                    "AND (:startDate IS NULL OR s.mjd_date_time >= CAST(:startDate AS TIMESTAMP)) " +
                    "AND (:endDate IS NULL OR s.mjd_date_time <= CAST(:endDate AS TIMESTAMP)) " +
                    "AND (:satLetter IS NULL OR LOWER(s.sat_letter) = LOWER(:satLetter))",
            countQuery = "SELECT count(*) FROM sat_combined_view_difference_materialized s " +
                    "WHERE (:#{#source2 == null} OR s.source2 IN (:source2)) " +
                    "AND (:startDate IS NULL OR s.mjd_date_time >= CAST(:startDate AS TIMESTAMP)) " +
                    "AND (:endDate IS NULL OR s.mjd_date_time <= CAST(:endDate AS TIMESTAMP)) " +
                    "AND (:satLetter IS NULL OR LOWER(s.sat_letter) = LOWER(:satLetter))",
            nativeQuery = true
    )
    Page<SatCombinedViewDifference> findOptimizedByLocationAndFilters(
            @Param("source2") List<String> source2,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            @Param("satLetter") String satLetter,
            Pageable pageable
    );




    @Query("SELECT s FROM SatCommonViewDifference s WHERE " +
            "(s.mjdDateTime >= COALESCE(:startDate, s.mjdDateTime)) AND " +
            "(s.mjdDateTime <= COALESCE(:endDate, s.mjdDateTime)) AND " +
            "(s.source1 = COALESCE(:source1, s.source1))")
    List<SatCommonViewDifference> findByFilters(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("source1") String source1
    );

}

