package com.time.tracealibility.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CellDataDTO {
    private boolean hasData;
    private int recordCount;
    private List<String> satellites;
    private Map<String, Integer> satelliteCount;
    private double completeness;
    private double avgRefSysDiff;
    private LocalDateTime firstRecord;
    private LocalDateTime lastRecord;
    private String status;
}
