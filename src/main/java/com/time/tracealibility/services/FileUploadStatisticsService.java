package com.time.tracealibility.services;

import com.time.tracealibility.dto.FileUploadStatsDTO;
import com.time.tracealibility.dto.FileUploadStatsDTO.*;
import com.time.tracealibility.entity.FileUploadStatistics;
import com.time.tracealibility.repository.FileUploadStatisticsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FileUploadStatisticsService {

    @Autowired
    private FileUploadStatisticsRepository repository;

    /**
     * Generate comprehensive file upload statistics report
     */
    public FileUploadStatsDTO generateFileUploadReport(LocalDateTime startDate, LocalDateTime endDate) {
        FileUploadStatsDTO report = new FileUploadStatsDTO();
        
        // Generate all sections of the report
        report.setSummary(generateSummaryStats(startDate, endDate));
        report.setDailyStats(generateDailyStats(startDate, endDate));
        report.setLocationStats(generateLocationStats(startDate, endDate));
        report.setFileTypeStats(generateFileTypeStats(startDate, endDate));
        report.setHourlyPatterns(generateHourlyPatterns(startDate, endDate));
        report.setMissingFiles(generateMissingFilesAnalysis(startDate, endDate));
        report.setPerformanceStats(generatePerformanceStats(startDate, endDate));
        report.setRecentUploads(generateRecentUploads());
        report.setFilesWithErrors(generateFilesWithErrors(startDate, endDate));
        
        return report;
    }

    private SummaryStats generateSummaryStats(LocalDateTime startDate, LocalDateTime endDate) {
        Long totalFiles = repository.countFilesByDateRange(startDate, endDate);
        Long totalRecords = repository.sumRecordsByDateRange(startDate, endDate);
        Double averageQuality = repository.averageQualityByDateRange(startDate, endDate);
        
        // Count unique locations
        List<FileUploadStatistics> files = repository.findByUploadTimestampBetweenOrderByUploadTimestampDesc(startDate, endDate);
        Integer uniqueLocations = (int) files.stream()
            .map(FileUploadStatistics::getLocationName)
            .distinct()
            .count();

        return new SummaryStats(
            totalFiles != null ? totalFiles : 0L,
            totalRecords != null ? totalRecords : 0L,
            averageQuality != null ? averageQuality : 0.0,
            uniqueLocations,
            startDate,
            endDate,
            LocalDateTime.now()
        );
    }

    private List<DailyUploadStats> generateDailyStats(LocalDateTime startDate, LocalDateTime endDate) {
        List<Object[]> rawStats = repository.getDailyUploadStatistics(startDate, endDate);
        List<DailyUploadStats> dailyStats = new ArrayList<>();
        
        for (Object[] row : rawStats) {
            String uploadDate = row[0].toString();
            Long fileCount = ((Number) row[1]).longValue();
            Long totalRecords = row[2] != null ? ((Number) row[2]).longValue() : 0L;
            Integer uniqueLocations = ((Number) row[3]).intValue();
            Double averageQuality = row[4] != null ? ((Number) row[4]).doubleValue() : 0.0;
            
            // Calculate completeness percentage (this would need business logic)
            Double completenessPercentage = calculateDailyCompleteness(uploadDate);
            
            dailyStats.add(new DailyUploadStats(
                uploadDate,
                LocalDateTime.parse(uploadDate + "T00:00:00"),
                fileCount,
                totalRecords,
                uniqueLocations,
                averageQuality,
                completenessPercentage
            ));
        }
        
        return dailyStats;
    }

    private List<LocationUploadStats> generateLocationStats(LocalDateTime startDate, LocalDateTime endDate) {
        List<Object[]> rawStats = repository.getLocationUploadStatistics(startDate, endDate);
        List<LocationUploadStats> locationStats = new ArrayList<>();
        
        for (Object[] row : rawStats) {
            String locationName = (String) row[0];
            String source2Code = (String) row[1];
            Long fileCount = ((Number) row[2]).longValue();
            Long totalRecords = row[3] != null ? ((Number) row[3]).longValue() : 0L;
            LocalDateTime firstUpload = (LocalDateTime) row[4];
            LocalDateTime lastUpload = (LocalDateTime) row[5];
            Double averageQuality = row[6] != null ? ((Number) row[6]).doubleValue() : 0.0;
            Long successfulFiles = row[7] != null ? ((Number) row[7]).longValue() : 0L;
            Long failedFiles = row[8] != null ? ((Number) row[8]).longValue() : 0L;
            
            Double successRate = fileCount > 0 ? (successfulFiles.doubleValue() / fileCount.doubleValue()) * 100.0 : 0.0;
            String status = determineLocationStatus(firstUpload, lastUpload, successRate);
            
            locationStats.add(new LocationUploadStats(
                locationName,
                source2Code,
                fileCount,
                totalRecords,
                firstUpload,
                lastUpload,
                averageQuality,
                successfulFiles,
                failedFiles,
                successRate,
                status
            ));
        }
        
        return locationStats;
    }

    private List<FileTypeStats> generateFileTypeStats(LocalDateTime startDate, LocalDateTime endDate) {
        List<Object[]> rawStats = repository.getFileTypeStatistics(startDate, endDate);
        List<FileTypeStats> fileTypeStats = new ArrayList<>();
        
        // Calculate total files for percentage calculation
        Long totalFiles = repository.countFilesByDateRange(startDate, endDate);
        
        for (Object[] row : rawStats) {
            String fileType = (String) row[0];
            Long fileCount = ((Number) row[1]).longValue();
            Long totalRecords = row[2] != null ? ((Number) row[2]).longValue() : 0L;
            Double averageProcessingTime = row[3] != null ? ((Number) row[3]).doubleValue() : 0.0;
            
            Double percentage = totalFiles > 0 ? (fileCount.doubleValue() / totalFiles.doubleValue()) * 100.0 : 0.0;
            
            fileTypeStats.add(new FileTypeStats(
                fileType,
                fileCount,
                totalRecords,
                averageProcessingTime,
                percentage
            ));
        }
        
        return fileTypeStats;
    }

    private List<HourlyUploadPattern> generateHourlyPatterns(LocalDateTime startDate, LocalDateTime endDate) {
        List<Object[]> rawStats = repository.getHourlyUploadPattern(startDate, endDate);
        List<HourlyUploadPattern> hourlyPatterns = new ArrayList<>();
        
        // Calculate total files for percentage calculation
        Long totalFiles = repository.countFilesByDateRange(startDate, endDate);
        
        for (Object[] row : rawStats) {
            Integer hour = ((Number) row[0]).intValue();
            Long fileCount = ((Number) row[1]).longValue();
            Long totalRecords = row[2] != null ? ((Number) row[2]).longValue() : 0L;
            
            Double percentage = totalFiles > 0 ? (fileCount.doubleValue() / totalFiles.doubleValue()) * 100.0 : 0.0;
            String peakIndicator = determinePeakIndicator(percentage);
            
            hourlyPatterns.add(new HourlyUploadPattern(
                hour,
                fileCount,
                totalRecords,
                percentage,
                peakIndicator
            ));
        }
        
        return hourlyPatterns;
    }

    private List<MissingFileInfo> generateMissingFilesAnalysis(LocalDateTime startDate, LocalDateTime endDate) {
        List<Object[]> rawStats = repository.getMissingFileAnalysis(startDate, endDate);
        List<MissingFileInfo> missingFiles = new ArrayList<>();
        
        for (Object[] row : rawStats) {
            Integer mjd = ((Number) row[0]).intValue();
            String locationName = (String) row[1];
            String status = (String) row[2];
            
            if ("MISSING".equals(status)) {
                String expectedFileName = generateExpectedFileName(locationName, mjd);
                Integer daysMissing = calculateDaysMissing(mjd);
                String severity = determineMissingSeverity(daysMissing);
                
                missingFiles.add(new MissingFileInfo(
                    mjd,
                    locationName,
                    status,
                    expectedFileName,
                    daysMissing,
                    severity
                ));
            }
        }
        
        return missingFiles;
    }

    private List<ProcessingPerformanceStats> generatePerformanceStats(LocalDateTime startDate, LocalDateTime endDate) {
        List<Object[]> rawStats = repository.getProcessingPerformanceStats(startDate, endDate);
        List<ProcessingPerformanceStats> performanceStats = new ArrayList<>();
        
        for (Object[] row : rawStats) {
            String locationName = (String) row[0];
            Long fileCount = ((Number) row[1]).longValue();
            Double averageProcessingTime = row[2] != null ? ((Number) row[2]).doubleValue() : 0.0;
            Double minProcessingTime = row[3] != null ? ((Number) row[3]).doubleValue() : 0.0;
            Double maxProcessingTime = row[4] != null ? ((Number) row[4]).doubleValue() : 0.0;
            Double averageFileSizeMB = row[5] != null ? ((Number) row[5]).doubleValue() : 0.0;
            Double averageLinesProcessed = row[6] != null ? ((Number) row[6]).doubleValue() : 0.0;
            Long totalLinesSkipped = row[7] != null ? ((Number) row[7]).longValue() : 0L;
            
            String performanceGrade = calculatePerformanceGrade(averageProcessingTime, averageFileSizeMB);
            
            performanceStats.add(new ProcessingPerformanceStats(
                locationName,
                fileCount,
                averageProcessingTime,
                minProcessingTime,
                maxProcessingTime,
                averageFileSizeMB,
                averageLinesProcessed,
                totalLinesSkipped,
                performanceGrade
            ));
        }
        
        return performanceStats;
    }

    private List<RecentFileUpload> generateRecentUploads() {
        List<FileUploadStatistics> recentFiles = repository.findRecentFiles();
        
        return recentFiles.stream().map(file -> new RecentFileUpload(
            file.getFileName(),
            file.getLocationName(),
            file.getSource2Code(),
            file.getMjd(),
            file.getUploadTimestamp(),
            file.getTotalRecords(),
            file.getFileStatus(),
            file.getDataQualityScore(),
            file.getSatellites(),
            file.getProcessingTimeMs()
        )).collect(Collectors.toList());
    }

    private List<FileWithErrors> generateFilesWithErrors(LocalDateTime startDate, LocalDateTime endDate) {
        List<FileUploadStatistics> errorFiles = repository.findFilesWithErrors(startDate, endDate);
        
        return errorFiles.stream().map(file -> {
            Double errorRate = file.getLinesProcessed() > 0 ? 
                (file.getLinesSkipped().doubleValue() / file.getLinesProcessed().doubleValue()) * 100.0 : 0.0;
            String severity = determineErrorSeverity(errorRate);
            
            return new FileWithErrors(
                file.getFileName(),
                file.getLocationName(),
                file.getMjd(),
                file.getUploadTimestamp(),
                file.getTotalRecords(),
                file.getLinesSkipped(),
                file.getProcessingErrors(),
                errorRate,
                severity
            );
        }).collect(Collectors.toList());
    }

    // Helper methods
    private Double calculateDailyCompleteness(String uploadDate) {
        // Implement business logic to calculate expected vs actual files for the day
        return 85.0; // Placeholder
    }

    private String determineLocationStatus(LocalDateTime firstUpload, LocalDateTime lastUpload, Double successRate) {
        if (lastUpload != null && lastUpload.isAfter(LocalDateTime.now().minusHours(24))) {
            if (successRate > 95.0) return "ACTIVE";
            if (successRate > 80.0) return "INTERMITTENT";
        }
        return "INACTIVE";
    }

    private String determinePeakIndicator(Double percentage) {
        if (percentage > 10.0) return "PEAK";
        if (percentage > 5.0) return "NORMAL";
        return "LOW";
    }

    private String generateExpectedFileName(String locationName, Integer mjd) {
        // Generate expected filename based on location and MJD
        String locationCode = getLocationCode(locationName);
        return locationCode + mjd + ".dat"; // Simplified
    }

    private String getLocationCode(String locationName) {
        switch (locationName) {
            case "Bangalore": return "GZLMB1";
            case "Faridabad": return "GZLMF1";
            case "NPL": return "GZLI2P";
            case "Ahmedabad": return "GZLAHM1";
            case "Bhubaneshwar": return "GZLBBS1";
            case "DRC": return "GZLDEL1";
            case "Guwahati": return "GZLGHT1";
            default: return "UNKNOWN";
        }
    }

    private Integer calculateDaysMissing(Integer mjd) {
        // Calculate days since the MJD
        LocalDateTime mjdDate = convertMjdToDateTime(mjd);
        return (int) java.time.Duration.between(mjdDate, LocalDateTime.now()).toDays();
    }

    private LocalDateTime convertMjdToDateTime(Integer mjd) {
        // Convert MJD to LocalDateTime (simplified)
        return LocalDateTime.now().minusDays(mjd % 100); // Placeholder logic
    }

    private String determineMissingSeverity(Integer daysMissing) {
        if (daysMissing > 7) return "HIGH";
        if (daysMissing > 3) return "MEDIUM";
        return "LOW";
    }

    private String calculatePerformanceGrade(Double averageProcessingTime, Double averageFileSizeMB) {
        // Simple performance grading based on processing time per MB
        if (averageFileSizeMB == 0) return "F";
        
        Double timePerMB = averageProcessingTime / averageFileSizeMB;
        
        if (timePerMB < 100) return "A";
        if (timePerMB < 500) return "B";
        if (timePerMB < 1000) return "C";
        if (timePerMB < 2000) return "D";
        return "F";
    }

    private String determineErrorSeverity(Double errorRate) {
        if (errorRate > 10.0) return "HIGH";
        if (errorRate > 5.0) return "MEDIUM";
        return "LOW";
    }

    /**
     * Save file upload statistics (called during file processing)
     */
    public FileUploadStatistics saveFileUploadStatistics(FileUploadStatistics stats) {
        return repository.save(stats);
    }

    /**
     * Get statistics for a specific location
     */
    public List<FileUploadStatistics> getLocationStatistics(String locationName) {
        return repository.findByLocationNameOrderByUploadTimestampDesc(locationName);
    }

    /**
     * Get statistics for a specific MJD
     */
    public List<FileUploadStatistics> getMjdStatistics(Integer mjd) {
        return repository.findByMjdOrderByUploadTimestampDesc(mjd);
    }
}
