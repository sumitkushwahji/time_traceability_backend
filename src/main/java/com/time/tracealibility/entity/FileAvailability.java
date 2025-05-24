package com.time.tracealibility.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "file_availability")
public class FileAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String source; // e.g., 'NPL', 'BANGALORE'
    private int mjd; // Modified Julian Date
    private String status; // AVAILABLE, MISSING, LATE
    private String fileName;

    private LocalDateTime timestamp; // when it was seen

    public FileAvailability(Long id, String source, int mjd, String status, String fileName, LocalDateTime timestamp) {
        this.id = id;
        this.source = source;
        this.mjd = mjd;
        this.status = status;
        this.fileName = fileName;
        this.timestamp = timestamp;
    }

    public FileAvailability() {
    }

    @Override
    public String toString() {
        return "FileAvailability{" +
                "id=" + id +
                ", source='" + source + '\'' +
                ", mjd=" + mjd +
                ", status='" + status + '\'' +
                ", fileName='" + fileName + '\'' +
                ", timestamp=" + timestamp +
                '}';
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

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    // getters and setters

}