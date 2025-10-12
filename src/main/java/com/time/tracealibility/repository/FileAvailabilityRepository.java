package com.time.tracealibility.repository;

import com.time.tracealibility.entity.FileAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FileAvailabilityRepository extends JpaRepository<FileAvailability, Long> {
    Optional<FileAvailability> findBySourceAndMjd(String source, int mjd);
    List<FileAvailability> findByMjdBetween(int startMjd, int endMjd);


    @Query("SELECT fa FROM FileAvailability fa WHERE fa.source IN :sources AND fa.mjd BETWEEN :startMjd AND :endMjd")
    List<FileAvailability> findBySourceInAndMjdBetween(
            @Param("sources") List<String> sources,
            @Param("startMjd") int startMjd,
            @Param("endMjd") int endMjd
    );

  @Modifying
  @Transactional
  @Query(value = "INSERT INTO file_availability (source, mjd, status, file_name, file_creation_time, last_checked_timestamp) " + // <-- Add new columns
    "VALUES (:source, :mjd, :status, :fileName, :fileCreationTime, :lastCheckedTimestamp) " + // <-- Add new values
    "ON CONFLICT (source, mjd) DO UPDATE SET " +
    "status = EXCLUDED.status, " +
    "file_name = EXCLUDED.file_name, " +
    "file_creation_time = EXCLUDED.file_creation_time, " + // <-- Update on conflict
    "last_checked_timestamp = EXCLUDED.last_checked_timestamp", // <-- Update on conflict
    nativeQuery = true)
  void upsertFileAvailability(@Param("source") String source,
                              @Param("mjd") int mjd,
                              @Param("status") String status,
                              @Param("fileName") String fileName,
                              @Param("fileCreationTime") LocalDateTime fileCreationTime, // <-- Add parameter
                              @Param("lastCheckedTimestamp") LocalDateTime lastCheckedTimestamp); // <-- Rename for clarity
}

