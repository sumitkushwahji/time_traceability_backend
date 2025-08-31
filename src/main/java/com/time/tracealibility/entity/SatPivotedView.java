package com.time.tracealibility.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "sat_pivoted_view_materialized")
@Immutable // Use @Immutable for read-only views to improve performance
public class SatPivotedView {

  @Id
  private String id;

  @Column(name = "sat_letter")
  private String satLetter;

  @Column(name = "mjd")
  private int mjd;

  @Column(name = "mjd_date_time")
  private LocalDateTime mjdDateTime;

  @Column(name = "sttime")
  private String sttime;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "location_diffs", columnDefinition = "jsonb")
  private Map<String, Double> locationDiffs;

  // Getters and Setters
  public String getId() { return id; }
  public void setId(String id) { this.id = id; }
  public String getSatLetter() { return satLetter; }
  public void setSatLetter(String satLetter) { this.satLetter = satLetter; }
  public int getMjd() { return mjd; }
  public void setMjd(int mjd) { this.mjd = mjd; }
  public LocalDateTime getMjdDateTime() { return mjdDateTime; }
  public void setMjdDateTime(LocalDateTime mjdDateTime) { this.mjdDateTime = mjdDateTime; }
  public String getSttime() { return sttime; }
  public void setSttime(String sttime) { this.sttime = sttime; }
  public Map<String, Double> getLocationDiffs() { return locationDiffs; }
  public void setLocationDiffs(Map<String, Double> locationDiffs) { this.locationDiffs = locationDiffs; }
}
