package com.time.tracealibility.repository;

import com.time.tracealibility.entity.ProcessedFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcessedFileRepository extends JpaRepository<ProcessedFile, String> {
}
