package com.time.tracealibility.controllers;

import com.time.tracealibility.dto.FileStatusDTO;
import com.time.tracealibility.entity.FileAvailability;
import com.time.tracealibility.repository.FileAvailabilityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/status")
public class StatusController {

    @Autowired
    private FileAvailabilityRepository fileAvailabilityRepository;

    private static final LocalDate MJD_EPOCH = LocalDate.of(1858, 11, 17);

    @GetMapping("/file-availability")
    public ResponseEntity<List<FileStatusDTO>> getFileAvailability(
            @RequestParam List<String> sources,
            @RequestParam String startDate, // Expects "yyyy-MM-dd"
            @RequestParam String endDate    // Expects "yyyy-MM-dd"
    ) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
        int startMjd = (int) ChronoUnit.DAYS.between(MJD_EPOCH, LocalDate.parse(startDate, formatter));
        int endMjd = (int) ChronoUnit.DAYS.between(MJD_EPOCH, LocalDate.parse(endDate, formatter));

        List<FileAvailability> availabilities = fileAvailabilityRepository.findBySourceInAndMjdBetween(sources, startMjd, endMjd);

        List<FileStatusDTO> dtos = availabilities.stream()
                .map(fa -> new FileStatusDTO(fa.getSource(), fa.getMjd(), fa.getStatus(), fa.getFileName(), fa.getTimestamp()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }
}

