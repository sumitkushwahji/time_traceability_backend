# 🌟 File Processing Logic

Detailed documentation of how satellite data files are discovered, processed, and stored in the Time Traceability Backend.

## 🔄 Processing Overview

The file processing system continuously monitors directories for new IRNSS satellite data files, processes them incrementally, and stores the extracted data in the database.

```
File Discovery → Validation → Parsing → Data Extraction → Storage → Statistics
       ↓             ↓          ↓           ↓            ↓          ↓
   Directory     File Type   Content    MJD & Source   Database   Analytics
   Scanning      Check       Reading    Calculation    Storage    Update
```

## 📁 File System Structure

### Expected Directory Layout
```
/path/to/satellite/data/
├── IRLMF/                 # Location 1
│   ├── GZLI2P60.866      # Sample file (MJD 60866)
│   ├── GZLI2P60.867      # Next day file
│   └── ...
├── IRXYZ/                 # Location 2
│   ├── GZXYZ160.866      # Location 2 files
│   └── ...
└── IRABC/                 # Location 3
    ├── GZABC360.866      # Location 3 files
    └── ...
```

### File Naming Convention
Files follow the pattern: `GZ[SOURCE][SESSION][MJD]`

**Examples:**
- `GZLI2P60.866` → Source: `GZLI2P`, Session: `60`, MJD: `866` (interpreted as `60866`)
- `GZXYZ160.867` → Source: `GZXYZ1`, Session: `60`, MJD: `867` (interpreted as `60867`)

## 🔍 File Discovery Process

### Scheduled Directory Scanning
```java
@Scheduled(fixedRate = 300000) // Every 5 minutes
public void processAllLocations() {
    try {
        Path parentDir = Paths.get(parentFolder);
        Files.list(parentDir)
            .filter(Files::isDirectory)
            .parallel() // Parallel processing for performance
            .forEach(this::processLocationFolder);
    } catch (IOException e) {
        logger.error("Error accessing parent directory: {}", parentFolder, e);
    }
}
```

### Directory Processing Logic
```java
private void processLocationFolder(Path locationDir) {
    String locationName = locationDir.getFileName().toString();
    
    try {
        Files.list(locationDir)
            .filter(Files::isRegularFile)
            .filter(this::isValidDataFile)
            .sorted() // Process in chronological order
            .forEach(file -> processFile(file, locationName));
    } catch (IOException e) {
        logger.error("Error processing location folder: {}", locationName, e);
    }
}
```

### File Validation
```java
private boolean isValidDataFile(Path file) {
    String fileName = file.getFileName().toString();
    
    // Check file extension and naming pattern
    return fileName.matches("GZ[A-Z0-9]{4,6}\\d{2,3}\\.\\d{3,4}") 
        && Files.isReadable(file)
        && !fileName.startsWith(".");
}
```

## 📊 MJD Calculation Logic

### Modified Julian Date Extraction
The MJD (Modified Julian Date) is extracted from the filename using a sophisticated parsing algorithm:

```java
public static FileInfo extractSourceAndMJD(String fileName) {
    // Remove file extension
    String baseName = fileName.replaceAll("\\.[^.]*$", "");
    
    if (baseName.length() < 8) {
        throw new IllegalArgumentException("Filename too short: " + fileName);
    }
    
    // Extract source (first 6 characters)
    String source = baseName.substring(0, 6);
    
    // Extract MJD (concatenate all digits after position 6)
    StringBuilder mjdBuilder = new StringBuilder();
    for (int i = 6; i < baseName.length(); i++) {
        if (Character.isDigit(baseName.charAt(i))) {
            mjdBuilder.append(baseName.charAt(i));
        }
    }
    
    if (mjdBuilder.length() == 0) {
        throw new IllegalArgumentException("No MJD found in filename: " + fileName);
    }
    
    int mjd = Integer.parseInt(mjdBuilder.toString());
    return new FileInfo(source, mjd);
}
```

### MJD Examples
| Filename | Source | Extracted Digits | Final MJD |
|----------|--------|------------------|-----------|
| `GZLI2P60.866` | `GZLI2P` | `60866` | `60866` |
| `GZXYZ160.867` | `GZXYZ1` | `60867` | `60867` |
| `GZABC260.123` | `GZABC2` | `60123` | `60123` |

### MJD to Date Conversion
```java
public static LocalDateTime mjdToDateTime(int mjd) {
    // MJD epoch: November 17, 1858
    LocalDate mjdEpoch = LocalDate.of(1858, 11, 17);
    return mjdEpoch.plusDays(mjd).atStartOfDay();
}
```

## 📄 File Content Processing

### Incremental Processing
The system supports incremental processing to handle large files efficiently:

```java
private void processFile(Path file, String locationName) {
    String fileId = getRelativePath(file);
    ProcessedFile processedFile = getOrCreateProcessedFile(fileId, file);
    
    try {
        List<String> lines = readFileWithFallback(file);
        
        // Skip already processed lines
        int startLine = (int) processedFile.getLastProcessedLine();
        List<String> newLines = lines.subList(startLine, lines.size());
        
        FileInfo fileInfo = extractSourceAndMJD(file.getFileName().toString());
        
        for (int i = 0; i < newLines.size(); i++) {
            String line = newLines.get(i).trim();
            if (!line.isEmpty()) {
                processDataLine(line, fileInfo, startLine + i + 1);
            }
            
            // Update progress every 100 lines
            if ((i + 1) % 100 == 0) {
                updateProcessingProgress(processedFile, startLine + i + 1);
            }
        }
        
        // Mark as completed
        markFileCompleted(processedFile, lines.size());
        
    } catch (Exception e) {
        markFileFailed(processedFile, e.getMessage());
        logger.error("Error processing file: {}", file, e);
    }
}
```

### Character Encoding Handling
```java
private List<String> readFileWithFallback(Path file) throws IOException {
    try {
        // Try UTF-8 first
        return Files.readAllLines(file, StandardCharsets.UTF_8);
    } catch (MalformedInputException e) {
        logger.warn("UTF-8 encoding failed for {}, falling back to ISO-8859-1", file);
        // Fallback to ISO-8859-1
        return Files.readAllLines(file, StandardCharsets.ISO_8859_1);
    }
}
```

## 🔧 Data Line Processing

### Line Format Parsing
Each line in the data file contains satellite timing information:

```java
private void processDataLine(String line, FileInfo fileInfo, int lineNumber) {
    try {
        String[] parts = line.split("\\s+");
        if (parts.length < 8) {
            logger.warn("Insufficient data in line {}: {}", lineNumber, line);
            return;
        }
        
        IrnssData data = new IrnssData();
        data.setSource(fileInfo.getSource());
        data.setMjd(fileInfo.getMjd());
        data.setMjdDateTime(mjdToDateTime(fileInfo.getMjd()));
        
        // Parse session number (if available)
        if (parts.length > 0 && isNumeric(parts[0])) {
            data.setSessionNumber(Integer.parseInt(parts[0]));
        }
        
        // Parse satellite timing data
        parseSatelliteTimes(data, parts);
        
        // Validate data before saving
        if (isValidData(data)) {
            irnssDataRepository.save(data);
            updateFileAvailability(fileInfo, true);
        }
        
    } catch (Exception e) {
        logger.error("Error processing line {}: {}", lineNumber, line, e);
    }
}
```

### Satellite Time Parsing
```java
private void parseSatelliteTimes(IrnssData data, String[] parts) {
    try {
        // Satellite A (column 1)
        if (parts.length > 1 && isValidTimestamp(parts[1])) {
            data.setSatATime(Double.parseDouble(parts[1]));
        }
        
        // Satellite B (column 2)
        if (parts.length > 2 && isValidTimestamp(parts[2])) {
            data.setSatBTime(Double.parseDouble(parts[2]));
        }
        
        // Continue for satellites C through G
        // ...
        
    } catch (NumberFormatException e) {
        logger.warn("Invalid numeric format in satellite times: {}", Arrays.toString(parts));
    }
}
```

### Data Validation
```java
private boolean isValidData(IrnssData data) {
    // Check required fields
    if (data.getSource() == null || data.getSource().length() != 6) {
        return false;
    }
    
    if (data.getMjd() <= 0) {
        return false;
    }
    
    // Check if at least one satellite time is present
    return data.getSatATime() != null || 
           data.getSatBTime() != null || 
           data.getSatCTime() != null;
}

private boolean isValidTimestamp(String value) {
    try {
        double timestamp = Double.parseDouble(value);
        // Reasonable range check for satellite timestamps
        return timestamp > 0 && timestamp < 1e12;
    } catch (NumberFormatException e) {
        return false;
    }
}
```

## 📊 Progress Tracking

### Processing Status Management
```java
private ProcessedFile getOrCreateProcessedFile(String fileId, Path file) {
    return processedFileRepository.findById(fileId)
        .orElseGet(() -> {
            ProcessedFile pf = new ProcessedFile();
            pf.setId(fileId);
            pf.setFilePath(file.toString());
            pf.setProcessingStatus("PENDING");
            pf.setLastProcessedLine(0L);
            return processedFileRepository.save(pf);
        });
}

private void updateProcessingProgress(ProcessedFile processedFile, int lineNumber) {
    processedFile.setLastProcessedLine((long) lineNumber);
    processedFile.setProcessingStatus("PROCESSING");
    processedFileRepository.save(processedFile);
}

private void markFileCompleted(ProcessedFile processedFile, int totalLines) {
    processedFile.setLastProcessedLine((long) totalLines);
    processedFile.setProcessingStatus("COMPLETED");
    processedFile.setCompletedAt(LocalDateTime.now());
    processedFileRepository.save(processedFile);
}
```

### File Availability Tracking
```java
private void updateFileAvailability(FileInfo fileInfo, boolean isAvailable) {
    FileAvailability availability = fileAvailabilityRepository
        .findBySourceAndMjdAndSessionNumber(
            fileInfo.getSource(), 
            fileInfo.getMjd(), 
            fileInfo.getSessionNumber()
        )
        .orElseGet(() -> {
            FileAvailability fa = new FileAvailability();
            fa.setSource(fileInfo.getSource());
            fa.setMjd(fileInfo.getMjd());
            fa.setSessionNumber(fileInfo.getSessionNumber());
            return fa;
        });
    
    availability.setIsAvailable(isAvailable);
    availability.setLastChecked(LocalDateTime.now());
    
    if (isAvailable && availability.getFirstDetected() == null) {
        availability.setFirstDetected(LocalDateTime.now());
    }
    
    fileAvailabilityRepository.save(availability);
}
```

## ⚡ Performance Optimizations

### Parallel Processing
```java
// Process multiple directories in parallel
Files.list(parentDir)
    .filter(Files::isDirectory)
    .parallel()
    .forEach(this::processLocationFolder);

// Batch database operations
List<IrnssData> batchData = new ArrayList<>();
// ... collect data ...
if (batchData.size() >= BATCH_SIZE) {
    irnssDataRepository.saveAll(batchData);
    batchData.clear();
}
```

### Memory Management
```java
private void processLargeFile(Path file) {
    try (Scanner scanner = new Scanner(file, "UTF-8")) {
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            processDataLine(line, fileInfo, lineNumber++);
            
            // Periodic memory cleanup
            if (lineNumber % 1000 == 0) {
                System.gc(); // Suggest garbage collection
            }
        }
    }
}
```

### Error Recovery
```java
private void handleProcessingError(Path file, Exception e) {
    logger.error("Processing failed for file: {}", file, e);
    
    // Increment retry count
    ProcessedFile processedFile = processedFileRepository.findById(getRelativePath(file))
        .orElse(null);
    
    if (processedFile != null) {
        int retryCount = processedFile.getRetryCount() + 1;
        
        if (retryCount < MAX_RETRIES) {
            processedFile.setRetryCount(retryCount);
            processedFile.setProcessingStatus("PENDING");
            processedFile.setErrorMessage(e.getMessage());
        } else {
            processedFile.setProcessingStatus("FAILED");
            processedFile.setErrorMessage("Max retries exceeded: " + e.getMessage());
        }
        
        processedFileRepository.save(processedFile);
    }
}
```

## 📈 Monitoring and Metrics

### Processing Statistics
```java
@Component
public class ProcessingMetrics {
    private final AtomicLong filesProcessed = new AtomicLong(0);
    private final AtomicLong linesProcessed = new AtomicLong(0);
    private final AtomicLong processingErrors = new AtomicLong(0);
    
    public void recordFileProcessed() {
        filesProcessed.incrementAndGet();
    }
    
    public void recordLinesProcessed(long lines) {
        linesProcessed.addAndGet(lines);
    }
    
    public ProcessingStats getStats() {
        return new ProcessingStats(
            filesProcessed.get(),
            linesProcessed.get(),
            processingErrors.get()
        );
    }
}
```

### Health Monitoring
```java
@Component
public class FileProcessingHealthIndicator implements HealthIndicator {
    
    @Override
    public Health health() {
        long stuckFiles = processedFileRepository
            .countByProcessingStatusAndStartedAtBefore(
                "PROCESSING", 
                LocalDateTime.now().minusHours(1)
            );
        
        if (stuckFiles > 0) {
            return Health.down()
                .withDetail("stuckFiles", stuckFiles)
                .build();
        }
        
        return Health.up()
            .withDetail("lastProcessingTime", getLastProcessingTime())
            .build();
    }
}
```

## 🔧 Configuration Options

### Processing Configuration
```properties
# File processing settings
irnss.parent-folder=/path/to/satellite/data
irnss.processing.batch-size=100
irnss.processing.max-retries=3
irnss.processing.parallel-directories=true

# Scheduler settings
irnss.scheduler.enabled=true
irnss.scheduler.fixed-rate=300000  # 5 minutes
irnss.scheduler.initial-delay=60000  # 1 minute

# Performance settings
irnss.performance.max-file-size=100MB
irnss.performance.timeout-seconds=300
```

---

This file processing logic provides robust, scalable, and efficient handling of satellite data files with comprehensive error handling, progress tracking, and performance optimization.

**Next**: Review [MJD Calculations](MJD-Calculations.md) for detailed date conversion logic.
