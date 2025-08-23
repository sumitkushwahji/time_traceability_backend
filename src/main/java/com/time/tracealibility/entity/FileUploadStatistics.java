package com.time.tracealibility.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Minimal File Upload Statistics Entity
 */
@Entity
@Table(name = "file_upload_statistics")
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
    private LocalDateTime uploadTimestamp;
    private LocalDate dataDate;
    private Long totalRecords = 0L;
    private Double qualityScore = 0.0;

    // Constructors
    public FileUploadStatistics() {}

    public FileUploadStatistics(Long id, String fileName, String locationName, String source2Code,
                               Integer mjd, LocalDateTime uploadTimestamp, LocalDate dataDate,
                               Long totalRecords, Double qualityScore) {
        this.id = id;
        this.fileName = fileName;
        this.locationName = locationName;
        this.source2Code = source2Code;
        this.mjd = mjd;
        this.uploadTimestamp = uploadTimestamp;
        this.dataDate = dataDate;
        this.totalRecords = totalRecords;
        this.qualityScore = qualityScore;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }

    public String getSource2Code() { return source2Code; }
    public void setSource2Code(String source2Code) { this.source2Code = source2Code; }

    public Integer getMjd() { return mjd; }
    public void setMjd(Integer mjd) { this.mjd = mjd; }

    public LocalDateTime getUploadTimestamp() { return uploadTimestamp; }
    public void setUploadTimestamp(LocalDateTime uploadTimestamp) { this.uploadTimestamp = uploadTimestamp; }

    public LocalDate getDataDate() { return dataDate; }
    public void setDataDate(LocalDate dataDate) { this.dataDate = dataDate; }

    public Long getTotalRecords() { return totalRecords; }
    public void setTotalRecords(Long totalRecords) { this.totalRecords = totalRecords; }

    public Double getQualityScore() { return qualityScore; }
    public void setQualityScore(Double qualityScore) { this.qualityScore = qualityScore; }
}
