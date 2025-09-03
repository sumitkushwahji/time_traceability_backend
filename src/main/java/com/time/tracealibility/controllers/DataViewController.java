package com.time.tracealibility.controllers;

import com.time.tracealibility.dto.SourceSessionStatusDTO;
import com.time.tracealibility.entity.*;
import com.time.tracealibility.repository.IrnssDataRepository;
import com.time.tracealibility.repository.SatCommonViewDifferenceRepository;
import com.time.tracealibility.repository.SatPivotedViewRepository;
import com.time.tracealibility.services.IrnssDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/data")
public class DataViewController {

    @Autowired
    private IrnssDataRepository irnssDataRepository;

    @Autowired
    private IrnssDataService irnssDataService;


    @Autowired
    private SatCommonViewDifferenceRepository satCommonViewDifferenceRepository;

    @Autowired
    private SatPivotedViewRepository satPivotedViewRepository;



    // 🚀 NEW OPTIMIZED ENDPOINTS FOR PERFORMANCE

    /**
     * Fast bulk data endpoint - similar to frontend fast component strategy
     * Returns all data for location, frontend handles filtering
     */
    @GetMapping("/bulk-location-data")
    public ResponseEntity<java.util.Map<String, Object>> getBulkLocationData(
            @RequestParam List<String> source2,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate
    ) {
        List<SatCommonViewDifference> data = satCommonViewDifferenceRepository.findBulkByLocationAndDateRange(
            source2, startDate, endDate
        );

        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("data", data);
        response.put("totalElements", data.size());
        response.put("cached", true);

        return ResponseEntity.ok(response);
    }

    /**
     * Optimized paginated endpoint using only indexed columns
     */
    @GetMapping("/optimized-sat-differences")
    public ResponseEntity<Page<SatCombinedViewDifference>> getOptimizedSatDifferences(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(defaultValue = "mjdDateTime") String sortBy,
      @RequestParam(defaultValue = "desc") String sortDirection,
      @RequestParam(required = false) String startDate,
      @RequestParam(required = false) String endDate,
      @RequestParam(required = false) String satLetter,
      @RequestParam List<String> source2
    ) {
      //
      // --- FIX STARTS HERE ---
      //
      // Map the API's camelCase property name to the database's snake_case column name.
      String sortColumn = sortBy;
      if ("mjdDateTime".equalsIgnoreCase(sortBy)) {
        sortColumn = "mjd_date_time";
      }
      // Add other mappings here if you sort by other columns with different names.

      // Use the corrected sortColumn to build the Sort object.
      Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortColumn);
      //
      // --- FIX ENDS HERE ---
      //

      Pageable pageable = PageRequest.of(page, size, sort);

      Page<SatCombinedViewDifference> result = satCommonViewDifferenceRepository.findOptimizedByLocationAndFilters(
        source2, startDate, endDate, satLetter, pageable
      );

      return ResponseEntity.ok(result);
    }




    @GetMapping("/session-completeness")
    public ResponseEntity<List<SourceSessionStatusDTO>> getSessionCompleteness(
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String mjd,
            @RequestParam(required = false) Integer currentSessionCount,
            @RequestParam(required = false) Integer expectedSessionCount) {

        List<SourceSessionStatusDTO> result = irnssDataService.getSessionCompleteness(source, mjd, currentSessionCount, expectedSessionCount);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/available-mjds")
    public ResponseEntity<List<String>> getAvailableMjds() {
        List<Integer> mjds = irnssDataRepository.findAll().stream()
                .map(IrnssData::getMjd)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        List<String> mjdStrings = mjds.stream().map(String::valueOf).collect(Collectors.toList());
        return ResponseEntity.ok(mjdStrings);
    }


  @GetMapping("/pivoted-sat-data")
  public ResponseEntity<Page<SatPivotedView>> getPivotedSatData(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "15") int size,
    @RequestParam(defaultValue = "mjd_date_time") String sortBy,
    @RequestParam(defaultValue = "desc") String sortDirection,
    @RequestParam(required = false) String startDate,
    @RequestParam(required = false) String endDate,
    @RequestParam(required = false) String satLetter
  ) {
    Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
    Pageable pageable = PageRequest.of(page, size, sort);

    Page<SatPivotedView> result = satPivotedViewRepository.findByFilters(
      startDate, endDate, (satLetter != null && !satLetter.equalsIgnoreCase("ALL")) ? satLetter : null, pageable
    );

    return ResponseEntity.ok(result);
  }


}
