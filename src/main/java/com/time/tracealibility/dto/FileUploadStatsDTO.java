package com.time.tracealibility.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Comprehensive File Upload Statistics DTO
 * Contains all statistics related to file uploads across different locations and time periods
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadStatsDTO {

    // Summary statistics
    private SummaryStats summary;
    
    // Daily upload trends
    private List<DailyUploadStats> dailyStats;
    
    // Location-wise statistics
    private List<LocationUploadStats> locationStats;
    
    // File type distribution
    private List<FileTypeStats> fileTypeStats;
    
    // Upload patterns (hourly distribution)
    private List<HourlyUploadPattern> hourlyPatterns;
    
    // Missing files analysis
    private List<MissingFileInfo> missingFiles;
    
    // Processing performance metrics
    private List<ProcessingPerformanceStats> performanceStats;
    
    // Recent uploads
    private List<RecentFileUpload> recentUploads;
    
    // Files with errors
    private List<FileWithErrors> filesWithErrors;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SummaryStats {
        private Long totalFiles;
        private Long totalRecords;
        private Double averageQuality;
        private Integer uniqueLocations;
        private LocalDateTime reportPeriodStart;
        private LocalDateTime reportPeriodEnd;
        private LocalDateTime lastGenerated;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyUploadStats {
        private String uploadDate;
        private LocalDateTime dateTime;
        private Long fileCount;
        private Long totalRecords;
        private Integer uniqueLocations;
        private Double averageQuality;
        private Double completenessPercentage;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LocationUploadStats {
        private String locationName;
        private String source2Code;
        private Long fileCount;
        private Long totalRecords;
        private LocalDateTime firstUpload;
        private LocalDateTime lastUpload;
        private Double averageQuality;
        private Long successfulFiles;
        private Long failedFiles;
        private Double successRate;
        private String status; // ACTIVE, INACTIVE, INTERMITTENT
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FileTypeStats {
        private String fileType;
        private Long fileCount;
        private Long totalRecords;
        private Double averageProcessingTime;
        private Double percentage;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HourlyUploadPattern {
        private Integer hour;
        private Long fileCount;
        private Long totalRecords;
        private Double percentage;
        private String peakIndicator; // PEAK, NORMAL, LOW
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MissingFileInfo {
        private Integer mjd;
        private String locationName;
        private String status; // MISSING, LATE, EXPECTED
        private String expectedFileName;
        private Integer daysMissing;
        private String severity; // HIGH, MEDIUM, LOW
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProcessingPerformanceStats {
        private String locationName;
        private Long fileCount;
        private Double averageProcessingTime;
        private Double minProcessingTime;
        private Double maxProcessingTime;
        private Double averageFileSizeMB;
        private Double averageLinesProcessed;
        private Long totalLinesSkipped;
        private String performanceGrade; // A, B, C, D, F
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentFileUpload {
        private String fileName;
        private String locationName;
        private String source2Code;
        private Integer mjd;
        private LocalDateTime uploadTimestamp;
        private Integer totalRecords;
        private String fileStatus;
        private Double qualityScore;
        private String satellites;
        private Long processingTimeMs;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FileWithErrors {
        private String fileName;
        private String locationName;
        private Integer mjd;
        private LocalDateTime uploadTimestamp;
        private Integer totalRecords;
        private Integer linesSkipped;
        private String processingErrors;
        private Double errorRate;
        private String severity; // HIGH, MEDIUM, LOW
    }
}
