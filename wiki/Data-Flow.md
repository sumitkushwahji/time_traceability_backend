# 🌊 Data Flow

Comprehensive guide to understanding how data moves through the Time Traceability Backend system, from file ingestion to API responses, including processing pipelines and data transformations.

## 📋 Table of Contents

- [Data Flow Overview](#-data-flow-overview)
- [File Ingestion Pipeline](#-file-ingestion-pipeline)
- [Data Processing Stages](#-data-processing-stages)
- [Database Data Flow](#-database-data-flow)
- [API Data Flow](#-api-data-flow)
- [Scheduler Data Flow](#-scheduler-data-flow)
- [Error Handling Flow](#-error-handling-flow)
- [Performance Optimizations](#-performance-optimizations)
- [Monitoring Data Flow](#-monitoring-data-flow)
- [Data Lifecycle Management](#-data-lifecycle-management)

## 🎯 Data Flow Overview

The Time Traceability Backend processes satellite data through multiple interconnected stages:

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   File System   │    │   File Watcher  │    │   Processing    │
│                 │    │                 │    │     Queue       │
│ • Satellite     │───▶│ • Directory     │───▶│ • Batch Jobs    │
│   Data Files    │    │   Monitoring    │    │ • Error         │
│ • IRLMF, IRXYZ  │    │ • File Events   │    │   Handling      │
│ • Multiple      │    │ • Change        │    │ • Progress      │
│   Formats       │    │   Detection     │    │   Tracking      │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                        │                        │
         │                        │                        ▼
         │                        │              ┌─────────────────┐
         │                        │              │  Data Parser    │
         │                        │              │                 │
         │                        │              │ • MJD Extract   │
         │                        │              │ • Source Parse  │
         │                        │              │ • Content Read  │
         │                        │              │ • Validation    │
         │                        │              └─────────────────┘
         │                        │                        │
         │                        │                        ▼
         │                        │              ┌─────────────────┐
         │                        │              │    Database     │
         │                        │              │                 │
         │                        │              │ • Raw Data      │
         │                        │              │ • Processed     │
         │                        │              │ • Materialized  │
         │                        │              │   Views         │
         │                        │              └─────────────────┘
         │                        │                        │
         │                        │                        ▼
         │                        │              ┌─────────────────┐
         │                        │              │   API Layer     │
         │                        │              │                 │
         │                        │              │ • REST APIs     │
         │                        │              │ • Statistics    │
         │                        │              │ • Health Check  │
         │                        │              │ • Real-time     │
         │                        │              └─────────────────┘
         │                        │                        │
         └────────────────────────┼────────────────────────┘
                                  │
                    ┌─────────────▼─────────────┐
                    │      Frontend/Client      │
                    │                           │
                    │ • Dashboard              │
                    │ • Monitoring             │
                    │ • Reports                │
                    │ • Real-time Updates      │
                    └───────────────────────────┘
```

### Key Data Flow Principles

1. **Event-Driven**: File system changes trigger processing
2. **Batch Processing**: Files processed in configurable batches
3. **Error Recovery**: Failed files are retried with backoff
4. **Data Integrity**: Checksums and validation at each stage
5. **Real-time Monitoring**: Live status updates and metrics

## 📁 File Ingestion Pipeline

### 1. File Discovery and Monitoring

```java
// IrnssDataService.java - File monitoring and discovery
@Scheduled(fixedRateString = "${irnss.scheduler.fixed-rate}")
public void processFilesScheduled() {
    if (!isSchedulerEnabled()) {
        return;
    }
    
    try {
        log.info("Starting scheduled file processing...");
        
        // Discover new files
        List<Path> discoveredFiles = discoverNewFiles();
        log.info("Discovered {} new files", discoveredFiles.size());
        
        // Process in batches
        processFilesInBatches(discoveredFiles);
        
        // Update materialized views
        refreshMaterializedViews();
        
    } catch (Exception e) {
        log.error("Error during scheduled file processing", e);
    }
}

private List<Path> discoverNewFiles() {
    List<Path> newFiles = new ArrayList<>();
    
    // Scan each location directory
    for (String location : Arrays.asList("IRLMF", "IRXYZ", "IRABC")) {
        Path locationPath = Paths.get(parentFolder, location);
        
        if (Files.exists(locationPath)) {
            try (Stream<Path> files = Files.walk(locationPath, 1)) {
                files.filter(Files::isRegularFile)
                     .filter(this::isValidSatelliteFile)
                     .filter(this::isNotProcessed)
                     .forEach(newFiles::add);
            } catch (IOException e) {
                log.error("Error scanning directory: {}", locationPath, e);
            }
        }
    }
    
    return newFiles;
}
```

### 2. File Validation and Filtering

```java
private boolean isValidSatelliteFile(Path file) {
    String fileName = file.getFileName().toString();
    
    // Check file extension
    if (!fileName.toLowerCase().matches(".*\\.(24d|nav|obs)$")) {
        log.debug("Skipping file with invalid extension: {}", fileName);
        return false;
    }
    
    // Check file size
    try {
        long fileSize = Files.size(file);
        if (fileSize < MIN_FILE_SIZE_BYTES || fileSize > MAX_FILE_SIZE_BYTES) {
            log.warn("File size out of range: {} ({} bytes)", fileName, fileSize);
            return false;
        }
    } catch (IOException e) {
        log.error("Error checking file size: {}", fileName, e);
        return false;
    }
    
    // Check if file is still being written
    if (isFileBeingWritten(file)) {
        log.debug("File still being written, skipping: {}", fileName);
        return false;
    }
    
    return true;
}

private boolean isFileBeingWritten(Path file) {
    try {
        long currentSize = Files.size(file);
        Thread.sleep(100); // Brief pause
        long newSize = Files.size(file);
        return currentSize != newSize;
    } catch (IOException | InterruptedException e) {
        return true; // Assume file is being written if we can't check
    }
}
```

### 3. File Information Extraction

```java
private FileInfo extractFileInfo(Path filePath) {
    String fileName = filePath.getFileName().toString();
    String location = filePath.getParent().getFileName().toString();
    
    FileInfo fileInfo = new FileInfo();
    fileInfo.setFileName(fileName);
    fileInfo.setFilePath(filePath.toString());
    fileInfo.setLocation(location);
    
    // Extract source and MJD from filename
    extractSourceAndMJD(fileName, fileInfo);
    
    // Get file metadata
    try {
        BasicFileAttributes attrs = Files.readAttributes(filePath, BasicFileAttributes.class);
        fileInfo.setFileSize(attrs.size());
        fileInfo.setCreationTime(attrs.creationTime().toInstant());
        fileInfo.setLastModified(attrs.lastModifiedTime().toInstant());
    } catch (IOException e) {
        log.error("Error reading file attributes: {}", filePath, e);
    }
    
    return fileInfo;
}

private void extractSourceAndMJD(String fileName, FileInfo fileInfo) {
    try {
        // Extract source from first 6 characters of filename
        if (fileName.length() >= 6) {
            String source = fileName.substring(0, 6);
            fileInfo.setSource(source);
        }
        
        // Extract MJD from position 6 onwards (before first dot)
        if (fileName.length() > 6) {
            String mjdPart = fileName.substring(6);
            int dotIndex = mjdPart.indexOf('.');
            if (dotIndex > 0) {
                String mjdDigits = mjdPart.substring(0, dotIndex);
                // Concatenate all digits
                StringBuilder mjdBuilder = new StringBuilder();
                for (char c : mjdDigits.toCharArray()) {
                    if (Character.isDigit(c)) {
                        mjdBuilder.append(c);
                    }
                }
                
                if (mjdBuilder.length() > 0) {
                    int mjd = Integer.parseInt(mjdBuilder.toString());
                    fileInfo.setMjd(mjd);
                }
            }
        }
    } catch (Exception e) {
        log.error("Error extracting source and MJD from filename: {}", fileName, e);
    }
}
```

## 🔄 Data Processing Stages

### 1. File Reading and Parsing

```java
private void processFile(FileInfo fileInfo) {
    Path filePath = Paths.get(fileInfo.getFilePath());
    
    try {
        log.info("Processing file: {}", fileInfo.getFileName());
        
        // Record processing start
        ProcessedFile processedFile = recordProcessingStart(fileInfo);
        
        // Read and parse file content
        List<IrnssData> dataRecords = parseFileContent(filePath, fileInfo);
        
        // Validate parsed data
        validateDataRecords(dataRecords, fileInfo);
        
        // Save to database
        saveDataRecords(dataRecords);
        
        // Update processing status
        recordProcessingComplete(processedFile, dataRecords.size());
        
        log.info("Successfully processed file: {} ({} records)", 
                fileInfo.getFileName(), dataRecords.size());
        
    } catch (Exception e) {
        log.error("Error processing file: {}", fileInfo.getFileName(), e);
        recordProcessingError(fileInfo, e);
        throw new DataProcessingException("Failed to process file: " + fileInfo.getFileName(), e);
    }
}

private List<IrnssData> parseFileContent(Path filePath, FileInfo fileInfo) throws IOException {
    List<IrnssData> dataRecords = new ArrayList<>();
    
    // Try multiple character encodings
    List<Charset> charsets = Arrays.asList(
        StandardCharsets.UTF_8,
        StandardCharsets.ISO_8859_1,
        Charset.forName("windows-1252")
    );
    
    for (Charset charset : charsets) {
        try {
            dataRecords = parseWithCharset(filePath, fileInfo, charset);
            if (!dataRecords.isEmpty()) {
                log.debug("Successfully parsed file with charset: {}", charset);
                break;
            }
        } catch (Exception e) {
            log.debug("Failed to parse with charset {}: {}", charset, e.getMessage());
        }
    }
    
    if (dataRecords.isEmpty()) {
        throw new DataProcessingException("Unable to parse file with any supported charset");
    }
    
    return dataRecords;
}

private List<IrnssData> parseWithCharset(Path filePath, FileInfo fileInfo, Charset charset) 
        throws IOException {
    List<IrnssData> dataRecords = new ArrayList<>();
    
    try (BufferedReader reader = Files.newBufferedReader(filePath, charset)) {
        String line;
        int lineNumber = 0;
        
        while ((line = reader.readLine()) != null) {
            lineNumber++;
            
            // Skip empty lines and comments
            if (line.trim().isEmpty() || line.startsWith("#")) {
                continue;
            }
            
            try {
                IrnssData dataRecord = parseLine(line, fileInfo, lineNumber);
                if (dataRecord != null) {
                    dataRecords.add(dataRecord);
                }
            } catch (Exception e) {
                log.warn("Error parsing line {} in file {}: {}", 
                        lineNumber, fileInfo.getFileName(), e.getMessage());
                
                // Continue processing other lines unless too many errors
                if (shouldStopProcessing(lineNumber, dataRecords.size())) {
                    throw new DataProcessingException(
                        "Too many parsing errors, stopping file processing");
                }
            }
        }
    }
    
    return dataRecords;
}
```

### 2. Data Validation and Transformation

```java
private IrnssData parseLine(String line, FileInfo fileInfo, int lineNumber) {
    String[] parts = line.trim().split("\\s+");
    
    if (parts.length < 3) {
        throw new DataProcessingException("Insufficient data fields in line: " + lineNumber);
    }
    
    try {
        IrnssData data = new IrnssData();
        
        // Set file metadata
        data.setFileName(fileInfo.getFileName());
        data.setSource(fileInfo.getSource());
        data.setMjd(fileInfo.getMjd());
        data.setLineNumber(lineNumber);
        data.setProcessedAt(LocalDateTime.now());
        
        // Parse numeric data
        data.setObservationValue(Double.parseDouble(parts[1]));
        
        // Additional fields based on file format
        if (parts.length > 2) {
            data.setSecondaryValue(Double.parseDouble(parts[2]));
        }
        if (parts.length > 3) {
            data.setTertiaryValue(Double.parseDouble(parts[3]));
        }
        
        // Validate ranges
        validateDataRanges(data);
        
        return data;
        
    } catch (NumberFormatException e) {
        throw new DataProcessingException("Invalid numeric data in line: " + lineNumber, e);
    }
}

private void validateDataRanges(IrnssData data) {
    // Validate observation value ranges
    if (data.getObservationValue() < MIN_OBSERVATION_VALUE || 
        data.getObservationValue() > MAX_OBSERVATION_VALUE) {
        throw new DataProcessingException(
            "Observation value out of valid range: " + data.getObservationValue());
    }
    
    // Validate MJD range
    if (data.getMjd() < MIN_MJD || data.getMjd() > MAX_MJD) {
        throw new DataProcessingException("MJD out of valid range: " + data.getMjd());
    }
    
    // Additional validation rules based on source
    validateSourceSpecificRules(data);
}
```

### 3. Batch Processing and Database Persistence

```java
@Transactional
private void saveDataRecords(List<IrnssData> dataRecords) {
    if (dataRecords.isEmpty()) {
        return;
    }
    
    try {
        // Process in smaller batches for better performance
        int batchSize = processingConfig.getBatchSize();
        
        for (int i = 0; i < dataRecords.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, dataRecords.size());
            List<IrnssData> batch = dataRecords.subList(i, endIndex);
            
            // Save batch
            irnssDataRepository.saveAll(batch);
            
            // Flush to free memory
            if (i % (batchSize * 10) == 0) {
                entityManager.flush();
                entityManager.clear();
            }
            
            log.debug("Saved batch {}-{} of {} records", i, endIndex - 1, dataRecords.size());
        }
        
        log.info("Saved {} data records to database", dataRecords.size());
        
    } catch (Exception e) {
        log.error("Error saving data records to database", e);
        throw new DataProcessingException("Database save failed", e);
    }
}
```

## 🗄️ Database Data Flow

### 1. Raw Data Storage

```sql
-- irnss_data table stores raw parsed data
INSERT INTO irnss_data (
    source, mjd, observation_value, secondary_value, tertiary_value,
    file_name, line_number, processed_at
) VALUES (?, ?, ?, ?, ?, ?, ?, ?);

-- processed_file table tracks file processing status
INSERT INTO processed_file (
    file_name, file_path, processed_at, file_size, record_count, 
    processing_status, error_message
) VALUES (?, ?, ?, ?, ?, ?, ?);
```

### 2. Materialized View Processing

```java
@Scheduled(fixedRateString = "${app.scheduler.materialized-view.refresh-interval}")
public void refreshMaterializedViews() {
    if (!isEnabled()) {
        return;
    }
    
    String[] viewNames = {
        "sat_common_view_difference_materialized",
        "sat_pivoted_view_materialized",
        "sat_combined_view_difference_materialized"
    };
    
    for (String viewName : viewNames) {
        try {
            log.info("Refreshing materialized view: {}", viewName);
            
            long startTime = System.currentTimeMillis();
            refreshSingleView(viewName);
            long duration = System.currentTimeMillis() - startTime;
            
            log.info("Refreshed materialized view {} in {}ms", viewName, duration);
            
        } catch (Exception e) {
            log.error("Error refreshing materialized view: {}", viewName, e);
        }
    }
}

private void refreshSingleView(String viewName) {
    String sql = "REFRESH MATERIALIZED VIEW " + viewName;
    
    try {
        entityManager.createNativeQuery(sql).executeUpdate();
    } catch (Exception e) {
        log.error("Failed to refresh materialized view: {}", viewName, e);
        
        // Try to recreate view if refresh fails
        recreateMaterializedView(viewName);
    }
}
```

### 3. Data Aggregation and Statistics

```sql
-- Materialized view for file availability statistics
CREATE MATERIALIZED VIEW sat_common_view_difference_materialized AS
SELECT 
    source,
    mjd,
    COUNT(*) as record_count,
    AVG(observation_value) as avg_observation,
    MIN(observation_value) as min_observation,
    MAX(observation_value) as max_observation,
    STDDEV(observation_value) as std_observation,
    MAX(processed_at) as last_updated
FROM irnss_data 
GROUP BY source, mjd
WITH DATA;

-- Index for performance
CREATE INDEX idx_sat_common_view_source_mjd 
ON sat_common_view_difference_materialized(source, mjd);
```

## 🔌 API Data Flow

### 1. Request Processing Pipeline

```java
// Controller layer - request entry point
@GetMapping("/api/data/file-availability")
public ResponseEntity<ApiResponse<List<FileAvailability>>> getFileAvailability(
        @RequestParam(required = false) Integer mjd,
        @RequestParam(required = false) String source) {
    
    try {
        // Input validation
        validateApiParameters(mjd, source);
        
        // Delegate to service layer
        List<FileAvailability> data = dataService.getFileAvailability(mjd, source);
        
        // Transform to API response
        return ResponseEntity.ok(ApiResponse.success(data));
        
    } catch (ValidationException e) {
        return ResponseEntity.badRequest()
            .body(ApiResponse.error("Validation failed: " + e.getMessage()));
    } catch (Exception e) {
        log.error("Error retrieving file availability data", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("Internal server error"));
    }
}
```

### 2. Service Layer Data Processing

```java
@Service
@Transactional(readOnly = true)
public class DataService {
    
    @Cacheable("file-availability")
    public List<FileAvailability> getFileAvailability(Integer mjd, String source) {
        // Build dynamic query based on parameters
        Specification<FileAvailability> spec = buildFileAvailabilitySpec(mjd, source);
        
        // Execute query with materialized view
        List<FileAvailability> rawData = fileAvailabilityRepository.findAll(spec);
        
        // Transform and enrich data
        return rawData.stream()
            .map(this::enrichFileAvailabilityData)
            .collect(Collectors.toList());
    }
    
    private FileAvailability enrichFileAvailabilityData(FileAvailability data) {
        // Add computed fields
        data.setIsAvailable(data.getRecordCount() > 0);
        
        // Calculate file processing metrics
        if (data.getProcessedAt() != null) {
            Duration processingTime = Duration.between(
                data.getFileCreated(), data.getProcessedAt());
            data.setProcessingTimeMs(processingTime.toMillis());
        }
        
        // Add data quality indicators
        data.setDataQuality(calculateDataQuality(data));
        
        return data;
    }
}
```

### 3. Repository Layer Data Access

```java
@Repository
public interface FileAvailabilityRepository extends JpaRepository<FileAvailability, Long>, 
        JpaSpecificationExecutor<FileAvailability> {
    
    // Custom query using materialized view
    @Query(value = """
        SELECT 
            m.source,
            m.mjd,
            m.record_count,
            m.last_updated,
            p.file_name,
            p.file_size,
            p.processed_at
        FROM sat_common_view_difference_materialized m
        LEFT JOIN processed_file p ON p.file_name LIKE CONCAT(m.source, '%', m.mjd, '%')
        WHERE (:mjd IS NULL OR m.mjd = :mjd)
          AND (:source IS NULL OR m.source = :source)
        ORDER BY m.mjd DESC, m.source
        """, nativeQuery = true)
    List<Object[]> findFileAvailabilityData(@Param("mjd") Integer mjd, 
                                          @Param("source") String source);
    
    // Specification for dynamic queries
    static Specification<FileAvailability> hasSource(String source) {
        return (root, query, cb) -> 
            source == null ? null : cb.equal(root.get("source"), source);
    }
    
    static Specification<FileAvailability> hasMjd(Integer mjd) {
        return (root, query, cb) -> 
            mjd == null ? null : cb.equal(root.get("mjd"), mjd);
    }
}
```

## ⏰ Scheduler Data Flow

### 1. Scheduled Processing Cycle

```java
@Component
@EnableScheduling
public class FileProcessingScheduler {
    
    private final AtomicBoolean isProcessing = new AtomicBoolean(false);
    private volatile SchedulerStatus status = SchedulerStatus.STOPPED;
    private LocalDateTime lastRunTime;
    private int filesProcessedThisRun = 0;
    
    @Scheduled(fixedRateString = "${irnss.scheduler.fixed-rate}")
    public void scheduledProcessing() {
        if (!isSchedulerEnabled()) {
            return;
        }
        
        // Prevent concurrent execution
        if (!isProcessing.compareAndSet(false, true)) {
            log.info("Previous processing still running, skipping this cycle");
            return;
        }
        
        try {
            runProcessingCycle();
        } finally {
            isProcessing.set(false);
        }
    }
    
    private void runProcessingCycle() {
        log.info("Starting processing cycle at {}", LocalDateTime.now());
        
        status = SchedulerStatus.RUNNING;
        lastRunTime = LocalDateTime.now();
        filesProcessedThisRun = 0;
        
        try {
            // Discover new files
            List<Path> newFiles = discoverNewFiles();
            
            if (newFiles.isEmpty()) {
                log.info("No new files found for processing");
                status = SchedulerStatus.IDLE;
                return;
            }
            
            // Process files in batches
            processFilesInBatches(newFiles);
            
            // Refresh materialized views
            refreshMaterializedViews();
            
            // Update file availability cache
            updateFileAvailabilityCache();
            
            status = SchedulerStatus.COMPLETED;
            log.info("Processing cycle completed. Processed {} files", filesProcessedThisRun);
            
        } catch (Exception e) {
            status = SchedulerStatus.ERROR;
            log.error("Error during processing cycle", e);
        }
    }
}
```

### 2. Progress Tracking and Monitoring

```java
@Component
public class ProcessingProgressTracker {
    
    private final Map<String, ProcessingProgress> activeProcessing = new ConcurrentHashMap<>();
    
    public void startFileProcessing(String fileName, int totalLines) {
        ProcessingProgress progress = new ProcessingProgress();
        progress.setFileName(fileName);
        progress.setTotalLines(totalLines);
        progress.setStartTime(LocalDateTime.now());
        progress.setStatus(ProcessingStatus.IN_PROGRESS);
        
        activeProcessing.put(fileName, progress);
    }
    
    public void updateProgress(String fileName, int processedLines) {
        ProcessingProgress progress = activeProcessing.get(fileName);
        if (progress != null) {
            progress.setProcessedLines(processedLines);
            progress.setProgressPercentage(
                (double) processedLines / progress.getTotalLines() * 100);
            
            // Estimate completion time
            Duration elapsed = Duration.between(progress.getStartTime(), LocalDateTime.now());
            if (processedLines > 0) {
                long totalTimeMs = elapsed.toMillis() * progress.getTotalLines() / processedLines;
                progress.setEstimatedCompletion(
                    progress.getStartTime().plus(Duration.ofMillis(totalTimeMs)));
            }
        }
    }
    
    public void completeProcessing(String fileName, boolean success, String errorMessage) {
        ProcessingProgress progress = activeProcessing.get(fileName);
        if (progress != null) {
            progress.setEndTime(LocalDateTime.now());
            progress.setStatus(success ? ProcessingStatus.COMPLETED : ProcessingStatus.FAILED);
            progress.setErrorMessage(errorMessage);
            
            // Move to completed processing history
            completedProcessing.put(fileName, progress);
            activeProcessing.remove(fileName);
        }
    }
}
```

## ⚠️ Error Handling Flow

### 1. Error Classification and Recovery

```java
@Component
public class ErrorHandlingService {
    
    public void handleProcessingError(String fileName, Exception error) {
        ErrorType errorType = classifyError(error);
        ErrorSeverity severity = determineSeverity(errorType, fileName);
        
        // Log error with appropriate level
        logError(fileName, error, severity);
        
        // Record error in database
        recordError(fileName, errorType, error.getMessage(), severity);
        
        // Determine recovery action
        RecoveryAction action = determineRecoveryAction(errorType, severity);
        executeRecoveryAction(fileName, action);
        
        // Notify monitoring systems if critical
        if (severity == ErrorSeverity.CRITICAL) {
            notifyMonitoringSystems(fileName, error);
        }
    }
    
    private ErrorType classifyError(Exception error) {
        if (error instanceof FileNotFoundException) {
            return ErrorType.FILE_NOT_FOUND;
        } else if (error instanceof IOException) {
            return ErrorType.IO_ERROR;
        } else if (error instanceof DataProcessingException) {
            return ErrorType.DATA_PARSING_ERROR;
        } else if (error instanceof SQLException) {
            return ErrorType.DATABASE_ERROR;
        } else {
            return ErrorType.UNKNOWN_ERROR;
        }
    }
    
    private RecoveryAction determineRecoveryAction(ErrorType errorType, ErrorSeverity severity) {
        return switch (errorType) {
            case FILE_NOT_FOUND -> RecoveryAction.SKIP_FILE;
            case IO_ERROR -> severity == ErrorSeverity.CRITICAL ? 
                           RecoveryAction.STOP_PROCESSING : RecoveryAction.RETRY_LATER;
            case DATA_PARSING_ERROR -> RecoveryAction.QUARANTINE_FILE;
            case DATABASE_ERROR -> RecoveryAction.RETRY_WITH_BACKOFF;
            default -> RecoveryAction.MANUAL_INTERVENTION;
        };
    }
}
```

### 2. Retry Mechanism with Exponential Backoff

```java
@Component
public class RetryableProcessingService {
    
    private final Map<String, RetryContext> retryContexts = new ConcurrentHashMap<>();
    
    @Retryable(
        value = {DataProcessingException.class, SQLException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2.0)
    )
    public void processFileWithRetry(String fileName) throws DataProcessingException {
        RetryContext context = retryContexts.computeIfAbsent(fileName, k -> new RetryContext());
        context.incrementAttempt();
        
        try {
            log.info("Processing file {} (attempt {})", fileName, context.getAttemptCount());
            
            // Actual file processing
            processFile(fileName);
            
            // Success - remove retry context
            retryContexts.remove(fileName);
            
        } catch (Exception e) {
            log.error("Error processing file {} on attempt {}", 
                     fileName, context.getAttemptCount(), e);
            
            // If max retries exceeded, move to failed queue
            if (context.getAttemptCount() >= MAX_RETRY_ATTEMPTS) {
                moveToFailedQueue(fileName, e);
                retryContexts.remove(fileName);
            }
            
            throw e;
        }
    }
    
    @Recover
    public void recoverFromProcessingFailure(DataProcessingException ex, String fileName) {
        log.error("Final recovery for file {} after all retries failed", fileName, ex);
        
        // Record permanent failure
        recordPermanentFailure(fileName, ex);
        
        // Move file to failed directory
        moveToFailedDirectory(fileName);
        
        // Notify administrators
        notifyFailure(fileName, ex);
    }
}
```

## ⚡ Performance Optimizations

### 1. Parallel Processing

```java
@Service
public class ParallelFileProcessingService {
    
    private final ThreadPoolTaskExecutor taskExecutor;
    
    public void processFilesInParallel(List<Path> files) {
        // Group files by location for parallel processing
        Map<String, List<Path>> filesByLocation = files.stream()
            .collect(Collectors.groupingBy(path -> 
                path.getParent().getFileName().toString()));
        
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        
        for (Map.Entry<String, List<Path>> entry : filesByLocation.entrySet()) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                processLocationFiles(entry.getKey(), entry.getValue());
            }, taskExecutor);
            
            futures.add(future);
        }
        
        // Wait for all locations to complete
        CompletableFuture<Void> allOf = CompletableFuture.allOf(
            futures.toArray(new CompletableFuture[0]));
        
        try {
            allOf.get(30, TimeUnit.MINUTES); // Timeout after 30 minutes
        } catch (Exception e) {
            log.error("Error in parallel file processing", e);
            
            // Cancel remaining tasks
            futures.forEach(future -> future.cancel(true));
        }
    }
    
    private void processLocationFiles(String location, List<Path> files) {
        log.info("Processing {} files in location {}", files.size(), location);
        
        for (Path file : files) {
            try {
                processFile(file);
            } catch (Exception e) {
                log.error("Error processing file in parallel: {}", file, e);
                // Continue with other files
            }
        }
    }
}
```

### 2. Database Batch Operations

```java
@Repository
public class BatchDataRepository {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @Transactional
    public void batchInsertIrnssData(List<IrnssData> dataList) {
        int batchSize = 50;
        
        for (int i = 0; i < dataList.size(); i++) {
            entityManager.persist(dataList.get(i));
            
            if (i % batchSize == 0 && i > 0) {
                // Flush and clear to avoid memory issues
                entityManager.flush();
                entityManager.clear();
            }
        }
        
        // Final flush
        entityManager.flush();
        entityManager.clear();
    }
    
    @Transactional
    public void bulkUpdateProcessedFiles(List<String> fileNames, ProcessingStatus status) {
        String sql = """
            UPDATE processed_file 
            SET processing_status = :status, 
                updated_at = CURRENT_TIMESTAMP 
            WHERE file_name IN (:fileNames)
            """;
        
        entityManager.createNativeQuery(sql)
            .setParameter("status", status.name())
            .setParameter("fileNames", fileNames)
            .executeUpdate();
    }
}
```

## 📊 Monitoring Data Flow

### 1. Real-time Metrics Collection

```java
@Component
public class DataFlowMetrics {
    
    private final MeterRegistry meterRegistry;
    private final Counter filesProcessedCounter;
    private final Timer fileProcessingTimer;
    private final Gauge activeProcessingGauge;
    
    public DataFlowMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.filesProcessedCounter = Counter.builder("files.processed")
            .description("Number of files processed")
            .tag("status", "success")
            .register(meterRegistry);
        
        this.fileProcessingTimer = Timer.builder("file.processing.duration")
            .description("Time taken to process files")
            .register(meterRegistry);
        
        this.activeProcessingGauge = Gauge.builder("processing.active.files")
            .description("Number of files currently being processed")
            .register(meterRegistry, this, DataFlowMetrics::getActiveProcessingCount);
    }
    
    public void recordFileProcessed(String fileName, Duration processingTime, boolean success) {
        filesProcessedCounter.increment(Tags.of(
            "status", success ? "success" : "error",
            "source", extractSource(fileName)
        ));
        
        fileProcessingTimer.record(processingTime);
        
        // Record processing rate
        meterRegistry.gauge("processing.rate.files.per.minute", 
            calculateProcessingRate());
    }
    
    private double calculateProcessingRate() {
        // Calculate files processed in last minute
        return filesProcessedCounter.count() / 
               Duration.ofMinutes(1).toSeconds() * 60;
    }
}
```

### 2. Health Check Data Flow

```java
@Component
public class DataFlowHealthIndicator implements HealthIndicator {
    
    @Override
    public Health health() {
        Health.Builder builder = new Health.Builder();
        
        try {
            // Check database connectivity
            boolean dbHealthy = checkDatabaseHealth();
            
            // Check file system accessibility
            boolean fsHealthy = checkFileSystemHealth();
            
            // Check scheduler status
            boolean schedulerHealthy = checkSchedulerHealth();
            
            // Check data processing pipeline
            boolean pipelineHealthy = checkProcessingPipelineHealth();
            
            if (dbHealthy && fsHealthy && schedulerHealthy && pipelineHealthy) {
                builder.up();
            } else {
                builder.down();
            }
            
            // Add detailed health information
            builder.withDetail("database", dbHealthy ? "UP" : "DOWN")
                   .withDetail("fileSystem", fsHealthy ? "UP" : "DOWN")
                   .withDetail("scheduler", schedulerHealthy ? "UP" : "DOWN")
                   .withDetail("processingPipeline", pipelineHealthy ? "UP" : "DOWN")
                   .withDetail("lastProcessingTime", getLastProcessingTime())
                   .withDetail("filesInQueue", getFilesInQueueCount())
                   .withDetail("errorRate", getErrorRate());
            
        } catch (Exception e) {
            builder.down(e);
        }
        
        return builder.build();
    }
}
```

## 🔄 Data Lifecycle Management

### 1. Data Retention and Archival

```java
@Component
public class DataLifecycleManager {
    
    @Scheduled(cron = "0 2 * * * *") // Run daily at 2 AM
    public void manageDataLifecycle() {
        try {
            // Archive old processed files
            archiveOldProcessedFiles();
            
            // Clean up temporary data
            cleanupTemporaryData();
            
            // Compress old log files
            compressOldLogFiles();
            
            // Update data retention metrics
            updateRetentionMetrics();
            
        } catch (Exception e) {
            log.error("Error during data lifecycle management", e);
        }
    }
    
    private void archiveOldProcessedFiles() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(
            dataRetentionConfig.getRetentionDays());
        
        List<ProcessedFile> oldFiles = processedFileRepository
            .findByProcessedAtBefore(cutoffDate);
        
        for (ProcessedFile file : oldFiles) {
            try {
                // Move to archive storage
                archiveFile(file);
                
                // Update database record
                file.setArchived(true);
                file.setArchivedAt(LocalDateTime.now());
                processedFileRepository.save(file);
                
                log.info("Archived processed file: {}", file.getFileName());
                
            } catch (Exception e) {
                log.error("Error archiving file: {}", file.getFileName(), e);
            }
        }
    }
}
```

### 2. Data Quality Monitoring

```java
@Component
public class DataQualityMonitor {
    
    @EventListener
    public void onFileProcessed(FileProcessedEvent event) {
        // Perform data quality checks
        DataQualityReport report = performQualityChecks(event.getFileName());
        
        // Store quality metrics
        dataQualityRepository.save(report);
        
        // Alert if quality issues detected
        if (report.hasQualityIssues()) {
            alertOnQualityIssues(report);
        }
    }
    
    private DataQualityReport performQualityChecks(String fileName) {
        DataQualityReport report = new DataQualityReport();
        report.setFileName(fileName);
        report.setCheckTime(LocalDateTime.now());
        
        // Check for data completeness
        report.setCompletenessScore(calculateCompletenessScore(fileName));
        
        // Check for data consistency
        report.setConsistencyScore(calculateConsistencyScore(fileName));
        
        // Check for outliers
        report.setOutlierCount(countOutliers(fileName));
        
        // Overall quality score
        report.setOverallQualityScore(calculateOverallScore(report));
        
        return report;
    }
}
```

---

## 📞 Data Flow Support

For understanding and troubleshooting data flow:

1. **Processing Issues**: Check scheduler status and error logs
2. **Performance Problems**: Review parallel processing and batch configurations
3. **Data Quality**: Monitor validation and quality metrics
4. **Error Recovery**: Review retry mechanisms and error handling

**Related Documentation**: 
- [API Design](API-Design.md) for understanding API data flow
- [Testing Strategy](Testing-Strategy.md) for testing data flow components
- [Debugging Guide](Debugging-Guide.md) for troubleshooting data flow issues

---

**Data Flow Complete!** 🌊 Your satellite data moves efficiently through the processing pipeline with comprehensive monitoring and error handling.
