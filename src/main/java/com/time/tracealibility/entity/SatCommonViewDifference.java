package com.time.tracealibility.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Entity
@Table(name = "sat_common_view_difference")
@Immutable
public class SatCommonViewDifference {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "sat_letter")
    private String satLetter;

    @Column(name = "mjd")
    private int mjd;

    @Column(name = "common_sattelite")
    private int commonSattelite;

    @Column(name = "sttime")
    private String sttime;

    @Column(name = "mjd_date_time")
    private OffsetDateTime mjdDateTime;

    @Column(name = "source1")
    private String source1;

    @Column(name = "source2")
    private String source2;

    @Column(name = "avg1")
    private Double avg1;

    @Column(name = "avg2")
    private Double avg2;

    @Column(name = "avg_refsys_difference")
    private Double avgRefsysDifference;

    // Getters and setters


    @Override
    public String toString() {
        return "SatCommonViewDifference{" +
                "id='" + id + '\'' +
                ", satLetter='" + satLetter + '\'' +
                ", mjd=" + mjd +
                ", commonSattelite=" + commonSattelite +
                ", sttime='" + sttime + '\'' +
                ", mjdDateTime=" + mjdDateTime +
                ", source1='" + source1 + '\'' +
                ", source2='" + source2 + '\'' +
                ", avg1=" + avg1 +
                ", avg2=" + avg2 +
                ", avgRefsysDifference=" + avgRefsysDifference +
                '}';
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public int getCommonSattelite() {
        return commonSattelite;
    }

    public void setCommonSattelite(int commonSattelite) {
        this.commonSattelite = commonSattelite;
    }

    public String getSttime() {
        return sttime;
    }

    public void setSttime(String sttime) {
        this.sttime = sttime;
    }

    public OffsetDateTime getMjdDateTime() {
        return mjdDateTime;
    }

    public void setMjdDateTime(OffsetDateTime mjdDateTime) {
        this.mjdDateTime = mjdDateTime;
    }

    public String getSource1() {
        return source1;
    }

    public void setSource1(String source1) {
        this.source1 = source1;
    }

    public String getSource2() {
        return source2;
    }

    public void setSource2(String source2) {
        this.source2 = source2;
    }

    public Double getAvg1() {
        return avg1;
    }

    public void setAvg1(Double avg1) {
        this.avg1 = avg1;
    }

    public Double getAvg2() {
        return avg2;
    }

    public void setAvg2(Double avg2) {
        this.avg2 = avg2;
    }

    public Double getAvgRefsysDifference() {
        return avgRefsysDifference;
    }

    public void setAvgRefsysDifference(Double avgRefsysDifference) {
        this.avgRefsysDifference = avgRefsysDifference;
    }
}
