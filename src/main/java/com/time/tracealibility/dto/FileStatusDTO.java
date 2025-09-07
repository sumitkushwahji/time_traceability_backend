package com.time.tracealibility.dto;

import java.time.LocalDateTime;

public class FileStatusDTO {
    private String source;
    private int mjd;
    private String status;
    private String fileName;
    private LocalDateTime lastUpdated;

    public FileStatusDTO(String source, int mjd, String status, String fileName, LocalDateTime lastUpdated) {
        this.source = source;
        this.mjd = mjd;
        this.status = status;
        this.fileName = fileName;
        this.lastUpdated = lastUpdated;
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

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
