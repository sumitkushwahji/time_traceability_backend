package com.time.tracealibility.controllers;

import com.time.tracealibility.dto.PivotedSatDiffDTO;
import com.time.tracealibility.dto.SourceSessionStatusDTO;
import com.time.tracealibility.entity.*;
import com.time.tracealibility.repository.FileAvailabilityRepository;
import com.time.tracealibility.repository.IrnssDataRepository;
import com.time.tracealibility.repository.IrnssDataViewRepository;
import com.time.tracealibility.repository.SatCommonViewDifferenceRepository;
import com.time.tracealibility.services.IrnssDataService;
import com.time.tracealibility.services.SatCommonViewDifferenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/data")
@CrossOrigin
public class DataViewController {

    @Autowired
    private IrnssDataRepository irnssDataRepository;

    @Autowired
    private IrnssDataService irnssDataService;

    @Autowired
    private IrnssDataViewRepository irnssDataViewRepository;

    @Autowired
    private SatCommonViewDifferenceRepository satCommonViewDifferenceRepository;

    @Autowired
    private SatCommonViewDifferenceService service;

    @Autowired
    private FileAvailabilityRepository fileAvailabilityRepository;

    @GetMapping("/file-availability")
    public List<FileAvailability> getAll(
            @RequestParam int startMjd,
            @RequestParam int endMjd
    ) {
        return fileAvailabilityRepository.findByMjdBetween(startMjd, endMjd);
    }

    // --- IRNSS DATA (table) ---
    @GetMapping("/irnss")
    public List<IrnssData> getAllIrnssData() {
        return irnssDataRepository.findAll();
    }

    @PostMapping("/irnss")
    public IrnssData createIrnssData(@RequestBody IrnssData data) {
        return irnssDataRepository.save(data);
    }

    @GetMapping("/irnss/{id}")
    public IrnssData getIrnssDataById(@PathVariable Long id) {
        return irnssDataRepository.findById(id).orElseThrow();
    }

    @PutMapping("/irnss/{id}")
    public IrnssData updateIrnssData(@PathVariable Long id, @RequestBody IrnssData updatedData) {
        IrnssData existing = irnssDataRepository.findById(id).orElseThrow();
        updatedData.setId(id);
        return irnssDataRepository.save(updatedData);
    }

    @DeleteMapping("/irnss/{id}")
    public void deleteIrnssData(@PathVariable Long id) {
        irnssDataRepository.deleteById(id);
    }

    // --- VIEWS ---
    @GetMapping("/irnss-view")
    public List<IrnssDataView> getIrnssDataView() {
        return irnssDataViewRepository.findAll();
    }

    @GetMapping("/sat-differences")
    public Page<SatCommonViewDifference> getSatCommonDifferences(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String satLetter,
            @RequestParam(required = false) List<String> source2 // 🎯 Changed to List<String>
    ) {
        // Map frontend field names to database column names for native queries
        String dbSortField = mapSortFieldToDbColumn(sortBy);

        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(dbSortField).descending() : Sort.by(dbSortField).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        String safeSearch = (search == null || search.isBlank()) ? null : search.trim().toLowerCase();
        String safeStartDate = (startDate == null || startDate.isBlank()) ? null : startDate;
        String safeEndDate = (endDate == null || endDate.isBlank()) ? null : endDate;
        // 🎯 Ensure 'ALL' or blank/null from frontend maps to null for the query
        String safeSatLetter = (satLetter == null || satLetter.isBlank() || "ALL".equalsIgnoreCase(satLetter.trim())) ? null : satLetter.trim();

        // 🎯 If source2 list is empty, treat as null for the query
        List<String> safeSource2 = (source2 == null || source2.isEmpty()) ? null : source2;

        return satCommonViewDifferenceRepository.searchAllWithDateFilter(
                safeSearch, safeStartDate, safeEndDate, safeSatLetter, safeSource2, pageable
        );
    }

    /**
     * Map frontend field names to database column names for native queries
     */
    private String mapSortFieldToDbColumn(String frontendField) {
        switch (frontendField) {
            case "mjdDateTime":
                return "mjd_date_time";
            case "satLetter":
                return "sat_letter";
            case "commonSattelite":
                return "common_sattelite";
            case "avgRefsysDifference":
                return "avg_refsys_difference";
            default:
                return frontendField; // Return as-is for fields like "id", "mjd", "source1", "source2", etc.
        }
    }

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


    @GetMapping("/sat-differences-pivoted")
    public List<PivotedSatDiffDTO> getPivotedSatDifferences(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) String source1
    ) {
        return service.getPivotedSatDifferences(startDate, endDate, source1);
    }
//


}
