package com.time.tracealibility.dto;

import lombok.Data;

@Data
public class SourceSessionStatusDTO {
    private String source;
    private String mjd;
    private int currentSessionCount;
    private int expectedSessionCount;

    public SourceSessionStatusDTO(String source, Long mjd, Long currentSessionCount, Long expectedSessionCount) {
        this.source = source;
        this.mjd = String.valueOf(mjd); // Safely convert to String
        this.currentSessionCount = currentSessionCount.intValue();
        this.expectedSessionCount = expectedSessionCount.intValue();
    }

    public SourceSessionStatusDTO() {
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getMjd() {
        return mjd;
    }

    public void setMjd(String mjd) {
        this.mjd = mjd;
    }

    public int getCurrentSessionCount() {
        return currentSessionCount;
    }

    public void setCurrentSessionCount(int currentSessionCount) {
        this.currentSessionCount = currentSessionCount;
    }

    public int getExpectedSessionCount() {
        return expectedSessionCount;
    }

    public void setExpectedSessionCount(int expectedSessionCount) {
        this.expectedSessionCount = expectedSessionCount;
    }
}
