package com.time.tracealibility.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Comprehensive File Upload Statistics DTO
 * Contains all statistics related to file uploads across different locations and time periods
 */
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

    // Constructors
    public FileUploadStatsDTO() {}

    public FileUploadStatsDTO(SummaryStats summary, List<DailyUploadStats> dailyStats, 
                             List<LocationUploadStats> locationStats, List<FileTypeStats> fileTypeStats,
                             List<HourlyUploadPattern> hourlyPatterns, List<MissingFileInfo> missingFiles,
                             List<ProcessingPerformanceStats> performanceStats, List<RecentFileUpload> recentUploads,
                             List<FileWithErrors> filesWithErrors) {
        this.summary = summary;
        this.dailyStats = dailyStats;
        this.locationStats = locationStats;
        this.fileTypeStats = fileTypeStats;
        this.hourlyPatterns = hourlyPatterns;
        this.missingFiles = missingFiles;
        this.performanceStats = performanceStats;
        this.recentUploads = recentUploads;
        this.filesWithErrors = filesWithErrors;
    }

    // Getters and Setters
    public SummaryStats getSummary() { return summary; }
    public void setSummary(SummaryStats summary) { this.summary = summary; }

    public List<DailyUploadStats> getDailyStats() { return dailyStats; }
    public void setDailyStats(List<DailyUploadStats> dailyStats) { this.dailyStats = dailyStats; }

    public List<LocationUploadStats> getLocationStats() { return locationStats; }
    public void setLocationStats(List<LocationUploadStats> locationStats) { this.locationStats = locationStats; }

    public List<FileTypeStats> getFileTypeStats() { return fileTypeStats; }
    public void setFileTypeStats(List<FileTypeStats> fileTypeStats) { this.fileTypeStats = fileTypeStats; }

    public List<HourlyUploadPattern> getHourlyPatterns() { return hourlyPatterns; }
    public void setHourlyPatterns(List<HourlyUploadPattern> hourlyPatterns) { this.hourlyPatterns = hourlyPatterns; }

    public List<MissingFileInfo> getMissingFiles() { return missingFiles; }
    public void setMissingFiles(List<MissingFileInfo> missingFiles) { this.missingFiles = missingFiles; }

    public List<ProcessingPerformanceStats> getPerformanceStats() { return performanceStats; }
    public void setPerformanceStats(List<ProcessingPerformanceStats> performanceStats) { this.performanceStats = performanceStats; }

    public List<RecentFileUpload> getRecentUploads() { return recentUploads; }
    public void setRecentUploads(List<RecentFileUpload> recentUploads) { this.recentUploads = recentUploads; }

    public List<FileWithErrors> getFilesWithErrors() { return filesWithErrors; }
    public void setFilesWithErrors(List<FileWithErrors> filesWithErrors) { this.filesWithErrors = filesWithErrors; }

    public static class SummaryStats {
        private Long totalFiles;
        private Long totalRecords;
        private Double averageQuality;
        private Integer uniqueLocations;
        private LocalDateTime reportPeriodStart;
        private LocalDateTime reportPeriodEnd;
        private LocalDateTime lastGenerated;

        public SummaryStats() {}

        public SummaryStats(Long totalFiles, Long totalRecords, Double averageQuality, 
                           Integer uniqueLocations, LocalDateTime reportPeriodStart, 
                           LocalDateTime reportPeriodEnd, LocalDateTime lastGenerated) {
            this.totalFiles = totalFiles;
            this.totalRecords = totalRecords;
            this.averageQuality = averageQuality;
            this.uniqueLocations = uniqueLocations;
            this.reportPeriodStart = reportPeriodStart;
            this.reportPeriodEnd = reportPeriodEnd;
            this.lastGenerated = lastGenerated;
        }

        // Getters and Setters
        public Long getTotalFiles() { return totalFiles; }
        public void setTotalFiles(Long totalFiles) { this.totalFiles = totalFiles; }

        public Long getTotalRecords() { return totalRecords; }
        public void setTotalRecords(Long totalRecords) { this.totalRecords = totalRecords; }

        public Double getAverageQuality() { return averageQuality; }
        public void setAverageQuality(Double averageQuality) { this.averageQuality = averageQuality; }

        public Integer getUniqueLocations() { return uniqueLocations; }
        public void setUniqueLocations(Integer uniqueLocations) { this.uniqueLocations = uniqueLocations; }

        public LocalDateTime getReportPeriodStart() { return reportPeriodStart; }
        public void setReportPeriodStart(LocalDateTime reportPeriodStart) { this.reportPeriodStart = reportPeriodStart; }

        public LocalDateTime getReportPeriodEnd() { return reportPeriodEnd; }
        public void setReportPeriodEnd(LocalDateTime reportPeriodEnd) { this.reportPeriodEnd = reportPeriodEnd; }

        public LocalDateTime getLastGenerated() { return lastGenerated; }
        public void setLastGenerated(LocalDateTime lastGenerated) { this.lastGenerated = lastGenerated; }
    }

    public static class DailyUploadStats {
        private String uploadDate;
        private LocalDateTime dateTime;
        private Long fileCount;
        private Long totalRecords;
        private Integer uniqueLocations;
        private Double averageQuality;
        private Double completenessPercentage;

        public DailyUploadStats() {}

        public DailyUploadStats(String uploadDate, LocalDateTime dateTime, Long fileCount, 
                               Long totalRecords, Integer uniqueLocations, Double averageQuality, 
                               Double completenessPercentage) {
            this.uploadDate = uploadDate;
            this.dateTime = dateTime;
            this.fileCount = fileCount;
            this.totalRecords = totalRecords;
            this.uniqueLocations = uniqueLocations;
            this.averageQuality = averageQuality;
            this.completenessPercentage = completenessPercentage;
        }

        // Getters and Setters
        public String getUploadDate() { return uploadDate; }
        public void setUploadDate(String uploadDate) { this.uploadDate = uploadDate; }

        public LocalDateTime getDateTime() { return dateTime; }
        public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }

        public Long getFileCount() { return fileCount; }
        public void setFileCount(Long fileCount) { this.fileCount = fileCount; }

        public Long getTotalRecords() { return totalRecords; }
        public void setTotalRecords(Long totalRecords) { this.totalRecords = totalRecords; }

        public Integer getUniqueLocations() { return uniqueLocations; }
        public void setUniqueLocations(Integer uniqueLocations) { this.uniqueLocations = uniqueLocations; }

        public Double getAverageQuality() { return averageQuality; }
        public void setAverageQuality(Double averageQuality) { this.averageQuality = averageQuality; }

        public Double getCompletenessPercentage() { return completenessPercentage; }
        public void setCompletenessPercentage(Double completenessPercentage) { this.completenessPercentage = completenessPercentage; }
    }

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

        public LocationUploadStats() {}

        public LocationUploadStats(String locationName, String source2Code, Long fileCount, 
                                  Long totalRecords, LocalDateTime firstUpload, LocalDateTime lastUpload,
                                  Double averageQuality, Long successfulFiles, Long failedFiles, 
                                  Double successRate, String status) {
            this.locationName = locationName;
            this.source2Code = source2Code;
            this.fileCount = fileCount;
            this.totalRecords = totalRecords;
            this.firstUpload = firstUpload;
            this.lastUpload = lastUpload;
            this.averageQuality = averageQuality;
            this.successfulFiles = successfulFiles;
            this.failedFiles = failedFiles;
            this.successRate = successRate;
            this.status = status;
        }

        // Getters and Setters
        public String getLocationName() { return locationName; }
        public void setLocationName(String locationName) { this.locationName = locationName; }

        public String getSource2Code() { return source2Code; }
        public void setSource2Code(String source2Code) { this.source2Code = source2Code; }

        public Long getFileCount() { return fileCount; }
        public void setFileCount(Long fileCount) { this.fileCount = fileCount; }

        public Long getTotalRecords() { return totalRecords; }
        public void setTotalRecords(Long totalRecords) { this.totalRecords = totalRecords; }

        public LocalDateTime getFirstUpload() { return firstUpload; }
        public void setFirstUpload(LocalDateTime firstUpload) { this.firstUpload = firstUpload; }

        public LocalDateTime getLastUpload() { return lastUpload; }
        public void setLastUpload(LocalDateTime lastUpload) { this.lastUpload = lastUpload; }

        public Double getAverageQuality() { return averageQuality; }
        public void setAverageQuality(Double averageQuality) { this.averageQuality = averageQuality; }

        public Long getSuccessfulFiles() { return successfulFiles; }
        public void setSuccessfulFiles(Long successfulFiles) { this.successfulFiles = successfulFiles; }

        public Long getFailedFiles() { return failedFiles; }
        public void setFailedFiles(Long failedFiles) { this.failedFiles = failedFiles; }

        public Double getSuccessRate() { return successRate; }
        public void setSuccessRate(Double successRate) { this.successRate = successRate; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public static class FileTypeStats {
        private String fileType;
        private Long fileCount;
        private Long totalRecords;
        private Double averageProcessingTime;
        private Double percentage;

        public FileTypeStats() {}

        public FileTypeStats(String fileType, Long fileCount, Long totalRecords, 
                            Double averageProcessingTime, Double percentage) {
            this.fileType = fileType;
            this.fileCount = fileCount;
            this.totalRecords = totalRecords;
            this.averageProcessingTime = averageProcessingTime;
            this.percentage = percentage;
        }

        // Getters and Setters
        public String getFileType() { return fileType; }
        public void setFileType(String fileType) { this.fileType = fileType; }

        public Long getFileCount() { return fileCount; }
        public void setFileCount(Long fileCount) { this.fileCount = fileCount; }

        public Long getTotalRecords() { return totalRecords; }
        public void setTotalRecords(Long totalRecords) { this.totalRecords = totalRecords; }

        public Double getAverageProcessingTime() { return averageProcessingTime; }
        public void setAverageProcessingTime(Double averageProcessingTime) { this.averageProcessingTime = averageProcessingTime; }

        public Double getPercentage() { return percentage; }
        public void setPercentage(Double percentage) { this.percentage = percentage; }
    }

    public static class HourlyUploadPattern {
        private Integer hour;
        private Long fileCount;
        private Long totalRecords;
        private Double percentage;
        private String peakIndicator; // PEAK, NORMAL, LOW

        public HourlyUploadPattern() {}

        public HourlyUploadPattern(Integer hour, Long fileCount, Long totalRecords, 
                                  Double percentage, String peakIndicator) {
            this.hour = hour;
            this.fileCount = fileCount;
            this.totalRecords = totalRecords;
            this.percentage = percentage;
            this.peakIndicator = peakIndicator;
        }

        // Getters and Setters
        public Integer getHour() { return hour; }
        public void setHour(Integer hour) { this.hour = hour; }

        public Long getFileCount() { return fileCount; }
        public void setFileCount(Long fileCount) { this.fileCount = fileCount; }

        public Long getTotalRecords() { return totalRecords; }
        public void setTotalRecords(Long totalRecords) { this.totalRecords = totalRecords; }

        public Double getPercentage() { return percentage; }
        public void setPercentage(Double percentage) { this.percentage = percentage; }

        public String getPeakIndicator() { return peakIndicator; }
        public void setPeakIndicator(String peakIndicator) { this.peakIndicator = peakIndicator; }
    }

    public static class MissingFileInfo {
        private Integer mjd;
        private String locationName;
        private String status; // MISSING, LATE, EXPECTED
        private String expectedFileName;
        private Integer daysMissing;
        private String severity; // HIGH, MEDIUM, LOW

        public MissingFileInfo() {}

        public MissingFileInfo(Integer mjd, String locationName, String status, 
                              String expectedFileName, Integer daysMissing, String severity) {
            this.mjd = mjd;
            this.locationName = locationName;
            this.status = status;
            this.expectedFileName = expectedFileName;
            this.daysMissing = daysMissing;
            this.severity = severity;
        }

        // Getters and Setters
        public Integer getMjd() { return mjd; }
        public void setMjd(Integer mjd) { this.mjd = mjd; }

        public String getLocationName() { return locationName; }
        public void setLocationName(String locationName) { this.locationName = locationName; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getExpectedFileName() { return expectedFileName; }
        public void setExpectedFileName(String expectedFileName) { this.expectedFileName = expectedFileName; }

        public Integer getDaysMissing() { return daysMissing; }
        public void setDaysMissing(Integer daysMissing) { this.daysMissing = daysMissing; }

        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }
    }

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

        public ProcessingPerformanceStats() {}

        public ProcessingPerformanceStats(String locationName, Long fileCount, Double averageProcessingTime,
                                         Double minProcessingTime, Double maxProcessingTime, Double averageFileSizeMB,
                                         Double averageLinesProcessed, Long totalLinesSkipped, String performanceGrade) {
            this.locationName = locationName;
            this.fileCount = fileCount;
            this.averageProcessingTime = averageProcessingTime;
            this.minProcessingTime = minProcessingTime;
            this.maxProcessingTime = maxProcessingTime;
            this.averageFileSizeMB = averageFileSizeMB;
            this.averageLinesProcessed = averageLinesProcessed;
            this.totalLinesSkipped = totalLinesSkipped;
            this.performanceGrade = performanceGrade;
        }

        // Getters and Setters
        public String getLocationName() { return locationName; }
        public void setLocationName(String locationName) { this.locationName = locationName; }

        public Long getFileCount() { return fileCount; }
        public void setFileCount(Long fileCount) { this.fileCount = fileCount; }

        public Double getAverageProcessingTime() { return averageProcessingTime; }
        public void setAverageProcessingTime(Double averageProcessingTime) { this.averageProcessingTime = averageProcessingTime; }

        public Double getMinProcessingTime() { return minProcessingTime; }
        public void setMinProcessingTime(Double minProcessingTime) { this.minProcessingTime = minProcessingTime; }

        public Double getMaxProcessingTime() { return maxProcessingTime; }
        public void setMaxProcessingTime(Double maxProcessingTime) { this.maxProcessingTime = maxProcessingTime; }

        public Double getAverageFileSizeMB() { return averageFileSizeMB; }
        public void setAverageFileSizeMB(Double averageFileSizeMB) { this.averageFileSizeMB = averageFileSizeMB; }

        public Double getAverageLinesProcessed() { return averageLinesProcessed; }
        public void setAverageLinesProcessed(Double averageLinesProcessed) { this.averageLinesProcessed = averageLinesProcessed; }

        public Long getTotalLinesSkipped() { return totalLinesSkipped; }
        public void setTotalLinesSkipped(Long totalLinesSkipped) { this.totalLinesSkipped = totalLinesSkipped; }

        public String getPerformanceGrade() { return performanceGrade; }
        public void setPerformanceGrade(String performanceGrade) { this.performanceGrade = performanceGrade; }
    }

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

        public RecentFileUpload() {}

        public RecentFileUpload(String fileName, String locationName, String source2Code, Integer mjd,
                               LocalDateTime uploadTimestamp, Integer totalRecords, String fileStatus,
                               Double qualityScore, String satellites, Long processingTimeMs) {
            this.fileName = fileName;
            this.locationName = locationName;
            this.source2Code = source2Code;
            this.mjd = mjd;
            this.uploadTimestamp = uploadTimestamp;
            this.totalRecords = totalRecords;
            this.fileStatus = fileStatus;
            this.qualityScore = qualityScore;
            this.satellites = satellites;
            this.processingTimeMs = processingTimeMs;
        }

        // Getters and Setters
        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }

        public String getLocationName() { return locationName; }
        public void setLocationName(String locationName) { this.locationName = locationName; }

        public String getSource2Code() { return source2Code; }
        public void setSource2Code(String source2Code) { this.source2Code = source2Code; }

        public Integer getMjd() { return mjd; }
        public void setMjd(Integer mjd) { this.mjd = mjd; }

        public LocalDateTime getUploadTimestamp() { return uploadTimestamp; }
        public void setUploadTimestamp(LocalDateTime uploadTimestamp) { this.uploadTimestamp = uploadTimestamp; }

        public Integer getTotalRecords() { return totalRecords; }
        public void setTotalRecords(Integer totalRecords) { this.totalRecords = totalRecords; }

        public String getFileStatus() { return fileStatus; }
        public void setFileStatus(String fileStatus) { this.fileStatus = fileStatus; }

        public Double getQualityScore() { return qualityScore; }
        public void setQualityScore(Double qualityScore) { this.qualityScore = qualityScore; }

        public String getSatellites() { return satellites; }
        public void setSatellites(String satellites) { this.satellites = satellites; }

        public Long getProcessingTimeMs() { return processingTimeMs; }
        public void setProcessingTimeMs(Long processingTimeMs) { this.processingTimeMs = processingTimeMs; }
    }

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

        public FileWithErrors() {}

        public FileWithErrors(String fileName, String locationName, Integer mjd, LocalDateTime uploadTimestamp,
                             Integer totalRecords, Integer linesSkipped, String processingErrors,
                             Double errorRate, String severity) {
            this.fileName = fileName;
            this.locationName = locationName;
            this.mjd = mjd;
            this.uploadTimestamp = uploadTimestamp;
            this.totalRecords = totalRecords;
            this.linesSkipped = linesSkipped;
            this.processingErrors = processingErrors;
            this.errorRate = errorRate;
            this.severity = severity;
        }

        // Getters and Setters
        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }

        public String getLocationName() { return locationName; }
        public void setLocationName(String locationName) { this.locationName = locationName; }

        public Integer getMjd() { return mjd; }
        public void setMjd(Integer mjd) { this.mjd = mjd; }

        public LocalDateTime getUploadTimestamp() { return uploadTimestamp; }
        public void setUploadTimestamp(LocalDateTime uploadTimestamp) { this.uploadTimestamp = uploadTimestamp; }

        public Integer getTotalRecords() { return totalRecords; }
        public void setTotalRecords(Integer totalRecords) { this.totalRecords = totalRecords; }

        public Integer getLinesSkipped() { return linesSkipped; }
        public void setLinesSkipped(Integer linesSkipped) { this.linesSkipped = linesSkipped; }

        public String getProcessingErrors() { return processingErrors; }
        public void setProcessingErrors(String processingErrors) { this.processingErrors = processingErrors; }

        public Double getErrorRate() { return errorRate; }
        public void setErrorRate(Double errorRate) { this.errorRate = errorRate; }

        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }
    }
}
