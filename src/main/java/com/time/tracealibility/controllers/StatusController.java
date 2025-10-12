package com.time.tracealibility.controllers;

import com.time.tracealibility.dto.FileStatusDTO;
import com.time.tracealibility.entity.FileAvailability;
import com.time.tracealibility.repository.FileAvailabilityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
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

    @Value("${irnss.parent-folder}")
    private String parentFolder;

    private static final LocalDate MJD_EPOCH = LocalDate.of(1858, 11, 17);

    // File creation time is now handled in the service layer

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

        System.out.println("Parent folder configured as: " + parentFolder);
        availabilities.forEach(fa -> {
            System.out.println("Processing record - Source: " + fa.getSource() + 
                             ", MJD: " + fa.getMjd() + 
                             ", Filename: " + fa.getFileName() +
                             ", Last Checked: " + fa.getLastCheckedTimestamp() +
                             ", Creation Time: " + fa.getFileCreationTime());
        });

        return ResponseEntity.ok(availabilities.stream()
                .map(fa -> new FileStatusDTO(
                    fa.getSource(),
                    fa.getMjd(),
                    fa.getStatus(),
                    fa.getFileName(),
                    fa.getLastCheckedTimestamp(),
                    fa.getFileCreationTime()
                ))
                .collect(Collectors.toList()));


    }
}

