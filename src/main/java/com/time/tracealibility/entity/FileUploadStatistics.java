package com.time.tracealibility.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Minimal File Upload Statistics Entity
 */
@Entity
@Table(name = "file_upload_statistics")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String locationName;

    private String source2Code;
    private Integer mjd;
    
    // File system timestamps
    private LocalDateTime fileCreationTime;    // When file was created/received on PC
    private LocalDateTime uploadTimestamp;     // When file was processed/uploaded to DB
    
    private LocalDate dataDate;
    private Long totalRecords = 0L;
    private Double qualityScore = 0.0;
}
