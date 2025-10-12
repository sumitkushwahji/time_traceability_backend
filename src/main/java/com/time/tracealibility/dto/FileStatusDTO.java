package com.time.tracealibility.dto;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.ZoneId;

public class FileStatusDTO {
    private String source;
    private int mjd;
    private String status;
    private String fileName;
    private String lastUpdated;  // Changed to String to include timezone info
    private String fileCreationTime;  // Changed to String to include timezone info

    public FileStatusDTO(String source, int mjd, String status, String fileName, LocalDateTime lastUpdated, LocalDateTime fileCreationTime) {
        this.source = source;
        this.mjd = mjd;
        this.status = status;
        this.fileName = fileName;
        if (lastUpdated != null) {
            ZonedDateTime zdt = ZonedDateTime.of(lastUpdated, ZoneId.systemDefault());
            this.lastUpdated = zdt.toInstant().toString(); // Convert to ISO-8601 format
        } else {
            this.lastUpdated = null;
        }
        if (fileCreationTime != null) {
            ZonedDateTime zdt = ZonedDateTime.of(fileCreationTime, ZoneId.systemDefault());
            this.fileCreationTime = zdt.toInstant().toString(); // Convert to ISO-8601 format
        } else {
            this.fileCreationTime = null;
        }
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

    public String getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        if (lastUpdated != null) {
            ZonedDateTime zdt = ZonedDateTime.of(lastUpdated, ZoneId.systemDefault());
            this.lastUpdated = zdt.toInstant().toString();
        } else {
            this.lastUpdated = null;
        }
    }

    public String getFileCreationTime() {
        return fileCreationTime;
    }

    public void setFileCreationTime(LocalDateTime fileCreationTime) {
        if (fileCreationTime != null) {
            ZonedDateTime zdt = ZonedDateTime.of(fileCreationTime, ZoneId.systemDefault());
            this.fileCreationTime = zdt.toInstant().toString();
        } else {
            this.fileCreationTime = null;
        }
    }
}
