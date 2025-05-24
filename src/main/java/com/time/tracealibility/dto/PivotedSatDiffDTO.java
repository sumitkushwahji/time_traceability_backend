package com.time.tracealibility.dto;

import java.util.Map;

public class PivotedSatDiffDTO {
    private String satLetter;
    private int mjd;
    private String mjdDateTime;
    private String sttime;
    private Map<String, Double> locationDiffs;

    public PivotedSatDiffDTO(String satLetter, int mjd, String mjdDateTime, String sttime, Map<String, Double> locationDiffs) {
        this.satLetter = satLetter;
        this.mjd = mjd;
        this.mjdDateTime = mjdDateTime;
        this.sttime = sttime;
        this.locationDiffs = locationDiffs;
    }

    public String getSatLetter() {
        return satLetter;
    }

    public void setSatLetter(String satLetter) {
        this.satLetter = satLetter;
    }

    public int getMjd() {
        return mjd;
    }

    public void setMjd(int mjd) {
        this.mjd = mjd;
    }

    public String getMjdDateTime() {
        return mjdDateTime;
    }

    public void setMjdDateTime(String mjdDateTime) {
        this.mjdDateTime = mjdDateTime;
    }

    public String getSttime() {
        return sttime;
    }

    public void setSttime(String sttime) {
        this.sttime = sttime;
    }

    public Map<String, Double> getLocationDiffs() {
        return locationDiffs;
    }

    public void setLocationDiffs(Map<String, Double> locationDiffs) {
        this.locationDiffs = locationDiffs;
    }
// Getters and setters
}

