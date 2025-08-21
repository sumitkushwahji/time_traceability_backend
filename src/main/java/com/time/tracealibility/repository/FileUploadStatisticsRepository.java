package com.time.tracealibility.repository;

import com.time.tracealibility.entity.FileUploadStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FileUploadStatisticsRepository extends JpaRepository<FileUploadStatistics, Long> {

    // Basic queries
    List<FileUploadStatistics> findByLocationNameOrderByUploadTimestampDesc(String locationName);
    
    List<FileUploadStatistics> findByMjdOrderByUploadTimestampDesc(Integer mjd);
    
    List<FileUploadStatistics> findByUploadTimestampBetweenOrderByUploadTimestampDesc(
        LocalDateTime startDate, LocalDateTime endDate);

    List<FileUploadStatistics> findByLocationNameAndUploadTimestampBetweenOrderByUploadTimestampDesc(
        String locationName, LocalDateTime startDate, LocalDateTime endDate);

    // Statistics queries (only using available fields)
    @Query("SELECT COUNT(f) FROM FileUploadStatistics f WHERE f.uploadTimestamp BETWEEN :startDate AND :endDate")
    Long countFilesByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(f) FROM FileUploadStatistics f WHERE f.locationName = :location AND f.uploadTimestamp BETWEEN :startDate AND :endDate")
    Long countFilesByLocationAndDateRange(@Param("location") String location, 
                                        @Param("startDate") LocalDateTime startDate, 
                                        @Param("endDate") LocalDateTime endDate);

    @Query("SELECT SUM(f.totalRecords) FROM FileUploadStatistics f WHERE f.uploadTimestamp BETWEEN :startDate AND :endDate")
    Long sumRecordsByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT AVG(f.qualityScore) FROM FileUploadStatistics f WHERE f.uploadTimestamp BETWEEN :startDate AND :endDate")
    Double averageQualityByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Recent files
    @Query("SELECT f FROM FileUploadStatistics f ORDER BY f.uploadTimestamp DESC")
    List<FileUploadStatistics> findRecentFiles();

    // Get distinct locations
    @Query("SELECT DISTINCT f.locationName FROM FileUploadStatistics f")
    List<String> findDistinctLocations();
}
