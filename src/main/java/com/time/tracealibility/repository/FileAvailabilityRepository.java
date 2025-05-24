package com.time.tracealibility.repository;

import com.time.tracealibility.entity.FileAvailability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FileAvailabilityRepository extends JpaRepository<FileAvailability, Long> {
    Optional<FileAvailability> findBySourceAndMjd(String source, int mjd);
    List<FileAvailability> findByMjdBetween(int startMjd, int endMjd);
}

