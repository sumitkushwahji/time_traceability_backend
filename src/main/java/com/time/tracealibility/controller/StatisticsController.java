package com.time.tracealibility.controller;

import com.time.tracealibility.dto.FileUploadStatsDTO;
import com.time.tracealibility.entity.FileUploadStatistics;
import com.time.tracealibility.services.RealFileUploadStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/statistics")
@CrossOrigin(origins = {"http://localhost:4200", "http://127.0.0.1:4200"}, 
            methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS},
            allowCredentials = "true")
public class StatisticsController {

    @Autowired
    private RealFileUploadStatisticsService fileUploadStatsService;

    /**
     * Get comprehensive file upload statistics
     * Shows detailed information about which files were uploaded from which locations on which days
     */
    @GetMapping("/file-upload-stats")
    public ResponseEntity<FileUploadStatsDTO> getFileUploadStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        // Set default date range if not provided (last 30 days)
        if (startDate == null) {
            startDate = LocalDateTime.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }
        
        FileUploadStatsDTO stats = fileUploadStatsService.generateFileUploadReport();
        return ResponseEntity.ok(stats);
    }

    /**
     * Get file upload statistics for a specific location
     */
    @GetMapping("/file-upload-stats/location/{locationName}")
    public ResponseEntity<FileUploadStatsDTO> getLocationFileUploads(
            @PathVariable String locationName
    ) {
        FileUploadStatsDTO stats = fileUploadStatsService.getLocationStats(locationName);
        return ResponseEntity.ok(stats);
    }

    /**
     * Get file upload statistics for a specific MJD
     */
    @GetMapping("/file-upload-stats/mjd/{mjd}")
    public ResponseEntity<FileUploadStatsDTO> getMjdFileUploads(
            @PathVariable Integer mjd
    ) {
        FileUploadStatsDTO stats = fileUploadStatsService.getMjdStats(mjd);
        return ResponseEntity.ok(stats);
    }
}
