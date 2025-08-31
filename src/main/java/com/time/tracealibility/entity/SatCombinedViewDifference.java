package com.time.tracealibility.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

import java.time.LocalDateTime;

/**
 * Represents the data from the sat_combined_view_difference_materialized view.
 * This entity is immutable as it maps to a materialized view, not a transactional table.
 */
@Entity
@Table(name = "sat_combined_view_difference_materialized")
@Immutable
public class SatCombinedViewDifference {

  @Id
  @Column(name = "id")
  private String id;

  @Column(name = "sat_letter")
  private String satLetter;

  @Column(name = "mjd")
  private int mjd;

  @Column(name = "sttime")
  private String sttime;

  @Column(name = "mjd_date_time")
  private LocalDateTime mjdDateTime;

  @Column(name = "common_satellite_count") // FIX: Corrected column name
  private int commonSatelliteCount;

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

  // NEW: Added fields for weighted averages
  @Column(name = "weighted_avg1")
  private Double weightedAvg1;

  @Column(name = "weighted_avg2")
  private Double weightedAvg2;

  @Column(name = "weighted_avg_difference")
  private Double weightedAvgDifference;


  // --- Getters and Setters ---

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

  public String getSttime() {
    return sttime;
  }

  public void setSttime(String sttime) {
    this.sttime = sttime;
  }

  public LocalDateTime getMjdDateTime() {
    return mjdDateTime;
  }

  public void setMjdDateTime(LocalDateTime mjdDateTime) {
    this.mjdDateTime = mjdDateTime;
  }

  public int getCommonSatelliteCount() {
    return commonSatelliteCount;
  }

  public void setCommonSatelliteCount(int commonSatelliteCount) {
    this.commonSatelliteCount = commonSatelliteCount;
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

  public Double getWeightedAvg1() {
    return weightedAvg1;
  }

  public void setWeightedAvg1(Double weightedAvg1) {
    this.weightedAvg1 = weightedAvg1;
  }

  public Double getWeightedAvg2() {
    return weightedAvg2;
  }

  public void setWeightedAvg2(Double weightedAvg2) {
    this.weightedAvg2 = weightedAvg2;
  }

  public Double getWeightedAvgDifference() {
    return weightedAvgDifference;
  }

  public void setWeightedAvgDifference(Double weightedAvgDifference) {
    this.weightedAvgDifference = weightedAvgDifference;
  }
}
