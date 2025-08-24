package com.time.tracealibility.services;

import com.time.tracealibility.dto.FileUploadStatsDTO;
import com.time.tracealibility.entity.FileUploadStatistics;
import com.time.tracealibility.repository.FileUploadStatisticsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SimpleFileUploadStatisticsService {

    @Autowired
    private FileUploadStatisticsRepository fileUploadStatisticsRepository;

    public FileUploadStatsDTO generateFileUploadReport() {
        FileUploadStatsDTO report = new FileUploadStatsDTO();
        
        // Get date range for last 30 days
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(30);
        
        // Get all data for analysis
        List<FileUploadStatistics> allData = fileUploadStatisticsRepository.findByUploadTimestampBetweenOrderByUploadTimestampDesc(startDate, endDate);
        
        // If no data in last 30 days, get all data
        if (allData.isEmpty()) {
            allData = fileUploadStatisticsRepository.findAll();
        }
        
        // If still no data, create sample data for demonstration
        if (allData.isEmpty()) {
            allData = generateSampleData();
        }
        
        report.setSummary(generateSummaryStats(allData, startDate, endDate));
        report.setDailyStats(generateDailyStats(allData));
        report.setLocationStats(generateLocationStats(allData));
        report.setFileTypeStats(generateFileTypeStats(allData));
        report.setHourlyPatterns(generateHourlyPatterns(allData));
        report.setMissingFiles(new ArrayList<>()); // TODO: Implement missing file detection
        report.setPerformanceStats(generatePerformanceStats(allData));
        report.setRecentUploads(generateRecentUploads(allData));
        report.setFilesWithErrors(new ArrayList<>()); // TODO: Implement error detection
        
        return report;
    }

    /**
     * Generate sample data for demonstration when database is empty
     */
    private List<FileUploadStatistics> generateSampleData() {
        List<FileUploadStatistics> sampleData = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        
        String[] locations = {"IRNPLI", "GZLMB1", "GZLMB2", "GZLMF1", "GZLMF2", "GZLMA2"};
        String[] fileTypes = {"NPLI", "LMB", "LMF", "LMA"};
        
        // Generate data for the last 30 days
        for (int day = 0; day < 30; day++) {
            LocalDateTime dayTime = now.minusDays(day);
            
            // Generate 3-8 files per day per location
            for (String location : locations) {
                int filesPerDay = 3 + (int)(Math.random() * 6);
                
                for (int file = 0; file < filesPerDay; file++) {
                    FileUploadStatistics stat = new FileUploadStatistics();
                    stat.setId((long) sampleData.size() + 1);
                    
                    // Generate filename with correct MJD
                    String fileType = fileTypes[(int)(Math.random() * fileTypes.length)];
                    int mjd = 60880 + (29 - day); // Recent MJDs
                    // Fix: Use the correct MJD in filename instead of random number
                    String mjdInFilename = String.valueOf(mjd).substring(2); // Get last 3 digits of MJD
                    stat.setFileName(location.substring(0, 4) + fileType + mjdInFilename + "." + String.format("%03d", (int)(Math.random() * 1000)));
                    stat.setLocationName(location);
                    stat.setSource2Code(location);
                    stat.setMjd(mjd);
                    
                    // File creation time - when file physically arrived on PC
                    LocalDateTime fileCreationTime;
                    if (day == 0) {
                        // For today, files arrive throughout the day up to current time
                        int maxHour = now.getHour();
                        int randomHour = (int)(Math.random() * (maxHour + 1));
                        int randomMinute = (int)(Math.random() * 60);
                        if (randomHour == maxHour) {
                            randomMinute = Math.min(randomMinute, now.getMinute());
                        }
                        fileCreationTime = dayTime.withHour(randomHour).withMinute(randomMinute).withSecond(0).withNano(0);
                    } else {
                        // For past days, files could arrive any time
                        fileCreationTime = dayTime.withHour((int)(Math.random() * 24))
                                                  .withMinute((int)(Math.random() * 60))
                                                  .withSecond(0).withNano(0);
                    }
                    
                    // Upload/processing time - typically happens shortly after file arrives
                    // Add 5-30 minutes delay for processing
                    LocalDateTime uploadProcessingTime = fileCreationTime.plusMinutes(5 + (int)(Math.random() * 25));
                    
                    stat.setFileCreationTime(fileCreationTime);    // When file arrived on PC
                    stat.setUploadTimestamp(uploadProcessingTime); // When file was processed to DB
                    stat.setDataDate(fileCreationTime.toLocalDate());
                    
                    // Random metrics
                    stat.setTotalRecords((long)(500 + Math.random() * 1500));
                    stat.setQualityScore(75.0 + Math.random() * 20.0); // 75-95%
                    
                    sampleData.add(stat);
                }
            }
        }
        
        return sampleData;
    }

    private FileUploadStatsDTO.SummaryStats generateSummaryStats(List<FileUploadStatistics> data, LocalDateTime startDate, LocalDateTime endDate) {
        FileUploadStatsDTO.SummaryStats summary = new FileUploadStatsDTO.SummaryStats();
        summary.setTotalFiles((long) data.size());
        summary.setTotalRecords(data.stream().mapToLong(f -> f.getTotalRecords() != null ? f.getTotalRecords() : 0).sum());
        summary.setAverageQuality(data.stream().mapToDouble(f -> f.getQualityScore() != null ? f.getQualityScore() : 0.0).average().orElse(0.0));
        summary.setUniqueLocations((int) data.stream().map(FileUploadStatistics::getLocationName).distinct().count());
        summary.setReportPeriodStart(startDate);
        summary.setReportPeriodEnd(endDate);
        summary.setLastGenerated(LocalDateTime.now());
        return summary;
    }

    private List<FileUploadStatsDTO.DailyUploadStats> generateDailyStats(List<FileUploadStatistics> data) {
        Map<LocalDate, List<FileUploadStatistics>> dailyGroups = data.stream()
            .filter(f -> f.getFileCreationTime() != null)
            .collect(Collectors.groupingBy(f -> f.getFileCreationTime().toLocalDate()));
        
        return dailyGroups.entrySet().stream()
            .map(entry -> {
                LocalDate date = entry.getKey();
                List<FileUploadStatistics> dayData = entry.getValue();
                
                FileUploadStatsDTO.DailyUploadStats daily = new FileUploadStatsDTO.DailyUploadStats();
                daily.setUploadDate(date.toString());
                daily.setDateTime(date.atStartOfDay());
                daily.setFileCount((long) dayData.size());
                daily.setTotalRecords(dayData.stream().mapToLong(f -> f.getTotalRecords() != null ? f.getTotalRecords() : 0).sum());
                daily.setUniqueLocations((int) dayData.stream().map(FileUploadStatistics::getLocationName).distinct().count());
                daily.setAverageQuality(dayData.stream().mapToDouble(f -> f.getQualityScore() != null ? f.getQualityScore() : 0.0).average().orElse(0.0));
                daily.setCompletenessPercentage(85.0 + Math.random() * 10); // Mock completeness percentage
                
                return daily;
            })
            .sorted(Comparator.comparing(FileUploadStatsDTO.DailyUploadStats::getDateTime))
            .collect(Collectors.toList());
    }

    private List<FileUploadStatsDTO.LocationUploadStats> generateLocationStats(List<FileUploadStatistics> data) {
        Map<String, List<FileUploadStatistics>> locationGroups = data.stream()
            .collect(Collectors.groupingBy(FileUploadStatistics::getLocationName));
        
        return locationGroups.entrySet().stream()
            .map(entry -> {
                String location = entry.getKey();
                List<FileUploadStatistics> locationData = entry.getValue();
                
                FileUploadStatsDTO.LocationUploadStats loc = new FileUploadStatsDTO.LocationUploadStats();
                loc.setLocationName(location);
                loc.setSource2Code(locationData.get(0).getSource2Code());
                loc.setFileCount((long) locationData.size());
                loc.setTotalRecords(locationData.stream().mapToLong(f -> f.getTotalRecords() != null ? f.getTotalRecords() : 0).sum());
                
                // Get first and last file creation timestamps (when files arrived)
                List<LocalDateTime> timestamps = locationData.stream()
                    .map(FileUploadStatistics::getFileCreationTime)
                    .filter(Objects::nonNull)
                    .sorted()
                    .collect(Collectors.toList());
                
                if (!timestamps.isEmpty()) {
                    loc.setFirstUpload(timestamps.get(0));
                    loc.setLastUpload(timestamps.get(timestamps.size() - 1));
                }
                
                loc.setAverageQuality(locationData.stream().mapToDouble(f -> f.getQualityScore() != null ? f.getQualityScore() : 0.0).average().orElse(0.0));
                loc.setSuccessfulFiles((long) locationData.size()); // Assuming all files are successful for now
                loc.setFailedFiles(0L);
                loc.setSuccessRate(100.0);
                loc.setStatus(determineLocationStatus(locationData));
                
                return loc;
            })
            .sorted(Comparator.comparing(FileUploadStatsDTO.LocationUploadStats::getFileCount).reversed())
            .collect(Collectors.toList());
    }

    private String determineLocationStatus(List<FileUploadStatistics> locationData) {
        if (locationData.isEmpty()) return "INACTIVE";
        
        LocalDateTime lastUpload = locationData.stream()
            .map(FileUploadStatistics::getUploadTimestamp)
            .filter(Objects::nonNull)
            .max(LocalDateTime::compareTo)
            .orElse(null);
        
        if (lastUpload == null) return "INACTIVE";
        
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);
        return lastUpload.isAfter(threeDaysAgo) ? "ACTIVE" : "INACTIVE";
    }

    private List<FileUploadStatsDTO.FileTypeStats> generateFileTypeStats(List<FileUploadStatistics> data) {
        // Extract file types from file names
        Map<String, List<FileUploadStatistics>> typeGroups = data.stream()
            .collect(Collectors.groupingBy(f -> extractFileType(f.getFileName())));
        
        long totalFiles = data.size();
        
        return typeGroups.entrySet().stream()
            .map(entry -> {
                String fileType = entry.getKey();
                List<FileUploadStatistics> typeData = entry.getValue();
                
                FileUploadStatsDTO.FileTypeStats type = new FileUploadStatsDTO.FileTypeStats();
                type.setFileType(fileType);
                type.setFileCount((long) typeData.size());
                type.setTotalRecords(typeData.stream().mapToLong(f -> f.getTotalRecords() != null ? f.getTotalRecords() : 0).sum());
                type.setAverageProcessingTime(100.0 + Math.random() * 500); // Mock processing time
                type.setPercentage(totalFiles > 0 ? (typeData.size() * 100.0) / totalFiles : 0.0);
                
                return type;
            })
            .sorted(Comparator.comparing(FileUploadStatsDTO.FileTypeStats::getFileCount).reversed())
            .collect(Collectors.toList());
    }

    private String extractFileType(String fileName) {
        if (fileName == null) return "Unknown";
        
        // Extract meaningful file type patterns
        if (fileName.contains("NPLI")) return "NPLI Data";
        if (fileName.contains("LMB")) return "LMB Data";
        if (fileName.contains("LMA")) return "LMA Data";
        if (fileName.contains("LMF")) return "LMF Data";
        if (fileName.startsWith("GZ")) return "GPS Data";
        if (fileName.startsWith("IR")) return "IRNSS Data";
        
        return "Other";
    }

    private List<FileUploadStatsDTO.HourlyUploadPattern> generateHourlyPatterns(List<FileUploadStatistics> data) {
        Map<Integer, List<FileUploadStatistics>> hourlyGroups = data.stream()
            .filter(f -> f.getFileCreationTime() != null)
            .collect(Collectors.groupingBy(f -> f.getFileCreationTime().getHour()));
        
        long totalFiles = data.size();
        
        List<FileUploadStatsDTO.HourlyUploadPattern> patterns = new ArrayList<>();
        for (int hour = 0; hour < 24; hour++) {
            List<FileUploadStatistics> hourData = hourlyGroups.getOrDefault(hour, new ArrayList<>());
            
            FileUploadStatsDTO.HourlyUploadPattern pattern = new FileUploadStatsDTO.HourlyUploadPattern();
            pattern.setHour(hour);
            pattern.setFileCount((long) hourData.size());
            pattern.setTotalRecords(hourData.stream().mapToLong(f -> f.getTotalRecords() != null ? f.getTotalRecords() : 0).sum());
            pattern.setPercentage(totalFiles > 0 ? (hourData.size() * 100.0) / totalFiles : 0.0);
            
            // Determine peak indicator
            double percentage = pattern.getPercentage();
            if (percentage > 8.0) pattern.setPeakIndicator("PEAK");
            else if (percentage > 4.0) pattern.setPeakIndicator("NORMAL");
            else pattern.setPeakIndicator("LOW");
            
            patterns.add(pattern);
        }
        
        return patterns;
    }

    private List<FileUploadStatsDTO.ProcessingPerformanceStats> generatePerformanceStats(List<FileUploadStatistics> data) {
        Map<String, List<FileUploadStatistics>> locationGroups = data.stream()
            .collect(Collectors.groupingBy(FileUploadStatistics::getLocationName));
        
        return locationGroups.entrySet().stream()
            .map(entry -> {
                String location = entry.getKey();
                List<FileUploadStatistics> locationData = entry.getValue();
                
                FileUploadStatsDTO.ProcessingPerformanceStats perf = new FileUploadStatsDTO.ProcessingPerformanceStats();
                perf.setLocationName(location);
                perf.setFileCount((long) locationData.size());
                
                // Mock performance metrics
                double avgTime = 100 + Math.random() * 400;
                perf.setAverageProcessingTime(avgTime);
                perf.setMinProcessingTime(avgTime * 0.5);
                perf.setMaxProcessingTime(avgTime * 2.0);
                perf.setAverageFileSizeMB(0.5 + Math.random() * 2.0);
                perf.setAverageLinesProcessed(locationData.stream().mapToDouble(f -> f.getTotalRecords() != null ? f.getTotalRecords() : 0).average().orElse(0.0));
                perf.setTotalLinesSkipped(0L);
                
                // Assign performance grade
                if (avgTime < 150) perf.setPerformanceGrade("A");
                else if (avgTime < 250) perf.setPerformanceGrade("B");
                else if (avgTime < 350) perf.setPerformanceGrade("C");
                else if (avgTime < 450) perf.setPerformanceGrade("D");
                else perf.setPerformanceGrade("F");
                
                return perf;
            })
            .collect(Collectors.toList());
    }

    private List<FileUploadStatsDTO.RecentFileUpload> generateRecentUploads(List<FileUploadStatistics> data) {
        return data.stream()
            .filter(f -> f.getFileCreationTime() != null)
            .sorted(Comparator.comparing(FileUploadStatistics::getFileCreationTime).reversed())
            .limit(10)
            .map(f -> {
                FileUploadStatsDTO.RecentFileUpload recent = new FileUploadStatsDTO.RecentFileUpload();
                recent.setFileName(f.getFileName());
                recent.setLocationName(f.getLocationName());
                recent.setSource2Code(f.getSource2Code());
                recent.setMjd(f.getMjd());
                recent.setUploadTimestamp(f.getFileCreationTime());  // Show file creation time, not processing time
                recent.setTotalRecords(f.getTotalRecords() != null ? f.getTotalRecords().intValue() : 0);
                recent.setFileStatus("SUCCESS");
                recent.setQualityScore(f.getQualityScore());
                recent.setSatellites(extractSatellitesFromFileName(f.getFileName()));
                recent.setProcessingTimeMs((long) (100 + Math.random() * 500));
                return recent;
            })
            .collect(Collectors.toList());
    }

    private String extractSatellitesFromFileName(String fileName) {
        if (fileName == null) return "";
        if (fileName.contains("NPLI")) return "IRNSS";
        if (fileName.contains("GZ")) return "GPS";
        if (fileName.startsWith("IR")) return "IRNSS";
        return "Mixed";
    }

    public FileUploadStatsDTO getLocationStats(String location) {
        return generateFileUploadReport();
    }

    public FileUploadStatsDTO getMjdStats(Integer mjd) {
        return generateFileUploadReport();
    }
}
