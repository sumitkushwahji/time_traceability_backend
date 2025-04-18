package com.time.tracealibility.controllers;

import com.time.tracealibility.entity.IrnssData;
import com.time.tracealibility.entity.IrnssDataView;
import com.time.tracealibility.entity.SatCommonViewDifference;
import com.time.tracealibility.repository.IrnssDataRepository;
import com.time.tracealibility.repository.IrnssDataViewRepository;
import com.time.tracealibility.repository.SatCommonViewDifferenceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;


import java.time.*;
import java.util.List;

@RestController
@RequestMapping("/api/data")
@CrossOrigin
public class DataViewController {

    @Autowired
    private IrnssDataRepository irnssDataRepository;

    @Autowired
    private IrnssDataViewRepository irnssDataViewRepository;

    @Autowired
    private SatCommonViewDifferenceRepository satCommonViewDifferenceRepository;

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
            @RequestParam(defaultValue = "") String search,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endDate
    ) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        // Convert startDate and endDate to UTC midnight if needed
        if (startDate != null) {
            // Adjust the startDate to 00:00:00.000Z in UTC
            startDate = startDate.withHour(0).withMinute(0).withSecond(0).withNano(0);
        }

        if (endDate != null) {
            // Adjust the endDate to 23:59:59.999999999Z in UTC
            endDate = endDate.withHour(23).withMinute(59).withSecond(59).withNano(999999999);
        }

        // If no endDate provided, adjust the end date to the end of the selected day
        if (startDate != null && endDate == null) {
            endDate = startDate.plusDays(1).minusNanos(1); // End of the selected day
        }

        if (search == null || search.trim().isEmpty()) {
            if (startDate != null && endDate != null) {
                return satCommonViewDifferenceRepository.findByDateRange(startDate, endDate, pageable);
            }
            return satCommonViewDifferenceRepository.findAll(pageable);
        } else {
            return satCommonViewDifferenceRepository.searchAllWithDateRange(search.trim().toLowerCase(), startDate, endDate, pageable);
        }
    }








}
