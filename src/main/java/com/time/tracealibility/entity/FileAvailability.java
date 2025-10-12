package com.time.tracealibility.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "file_availability",
       uniqueConstraints = @UniqueConstraint(columnNames = {"source", "mjd"}))
public class FileAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String source; // e.g., 'NPL', 'BANGALORE'
    private int mjd; // Modified Julian Date
    private String status; // AVAILABLE, MISSING, LATE
    private String fileName;

  @Column(name = "file_creation_time", columnDefinition = "TIMESTAMP WITH TIME ZONE")
  private LocalDateTime fileCreationTime;

  @Column(name = "last_checked_timestamp", columnDefinition = "TIMESTAMP WITH TIME ZONE")
  private LocalDateTime lastCheckedTimestamp;

  public FileAvailability(Long id, String source, int mjd, String status, String fileName, LocalDateTime fileCreationTime, LocalDateTime lastCheckedTimestamp) {
    this.id = id;
    this.source = source;
    this.mjd = mjd;
    this.status = status;
    this.fileName = fileName;
    this.fileCreationTime = fileCreationTime; // <-- ADD THIS
    this.lastCheckedTimestamp = lastCheckedTimestamp; // <-- RENAME THIS
  }

    public FileAvailability() {
    }




    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public int getMjd() {
        return mjd;
    }

    public void setMjd(int mjd) {
        this.mjd = mjd;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

  public LocalDateTime getFileCreationTime() { // <-- ADD THIS
    return fileCreationTime;
  }

  public void setFileCreationTime(LocalDateTime fileCreationTime) { // <-- ADD THIS
    this.fileCreationTime = fileCreationTime;
  }

  public LocalDateTime getLastCheckedTimestamp() { // <-- RENAME THIS
    return lastCheckedTimestamp;
  }

  public void setLastCheckedTimestamp(LocalDateTime lastCheckedTimestamp) { // <-- RENAME THIS
    this.lastCheckedTimestamp = lastCheckedTimestamp;
  }
    // getters and setters

}
