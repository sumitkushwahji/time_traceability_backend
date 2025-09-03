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

