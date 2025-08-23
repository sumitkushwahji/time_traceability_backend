package com.time.tracealibility.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsSummaryDTO {
    private int totalRecords;
    private int uniqueLocations;
    private int uniqueSatellites;
    private int totalTimePeriods;
    private double overallCompleteness;
    private LocalDateTime dataRangeStart;
    private LocalDateTime dataRangeEnd;
}
