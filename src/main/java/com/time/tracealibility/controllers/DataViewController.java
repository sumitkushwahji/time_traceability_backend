package com.time.tracealibility.controllers;

import com.time.tracealibility.dto.SourceSessionStatusDTO;
import com.time.tracealibility.entity.FileAvailability;
import com.time.tracealibility.entity.IrnssData;
import com.time.tracealibility.entity.IrnssDataView;
import com.time.tracealibility.entity.SatCommonViewDifference;
import com.time.tracealibility.repository.FileAvailabilityRepository;
import com.time.tracealibility.repository.IrnssDataRepository;
import com.time.tracealibility.repository.IrnssDataViewRepository;
import com.time.tracealibility.repository.SatCommonViewDifferenceRepository;
import com.time.tracealibility.services.IrnssDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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
            @RequestParam(defaultValue = "") String search,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate
    ) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        // Call new query method
        return satCommonViewDifferenceRepository.searchAllWithDateFilter(
                search.trim().toLowerCase(),
                startDate,
                endDate,
                pageable
        );
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

//


}
