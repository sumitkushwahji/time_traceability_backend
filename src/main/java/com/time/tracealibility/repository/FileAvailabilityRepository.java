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
    @Query("UPDATE FileAvailability fa SET fa.status = :status, fa.fileName = :fileName, fa.timestamp = :timestamp " +
           "WHERE fa.source = :source AND fa.mjd = :mjd")
    int updateFileAvailability(@Param("source") String source, 
                              @Param("mjd") int mjd, 
                              @Param("status") String status, 
                              @Param("fileName") String fileName, 
                              @Param("timestamp") LocalDateTime timestamp);
}

