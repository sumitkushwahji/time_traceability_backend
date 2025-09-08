# 📏 Code Standards

Comprehensive coding conventions and best practices for the Time Traceability Backend project, ensuring consistent, maintainable, and high-quality code across the team.

## 📋 Table of Contents

- [General Principles](#-general-principles)
- [Java Code Standards](#-java-code-standards)
- [Spring Boot Conventions](#-spring-boot-conventions)
- [Database Standards](#-database-standards)
- [Testing Standards](#-testing-standards)
- [Documentation Standards](#-documentation-standards)
- [Security Standards](#-security-standards)
- [Performance Standards](#-performance-standards)
- [Code Review Standards](#-code-review-standards)
- [Quality Assurance](#-quality-assurance)

## 🎯 General Principles

### Core Development Principles

1. **Readability First**: Code is read more often than it's written
2. **Consistency**: Follow established patterns throughout the codebase
3. **Simplicity**: Prefer simple, clear solutions over complex ones
4. **Maintainability**: Write code that's easy to modify and extend
5. **Testability**: Design code that's easy to test
6. **Documentation**: Document complex logic and design decisions

### SOLID Principles

```java
// Single Responsibility Principle
public class FileProcessor {
    public void processFile(Path filePath) { /* Only processes files */ }
}

public class FileValidator {
    public boolean isValid(Path filePath) { /* Only validates files */ }
}

// Open/Closed Principle
public abstract class DataParser {
    public abstract List<IrnssData> parse(String content);
}

public class NavigationDataParser extends DataParser {
    @Override
    public List<IrnssData> parse(String content) { /* Implementation */ }
}

// Dependency Inversion Principle
public class FileProcessingService {
    private final FileRepository repository;
    private final FileValidator validator;
    
    public FileProcessingService(FileRepository repository, FileValidator validator) {
        this.repository = repository;
        this.validator = validator;
    }
}
```

## ☕ Java Code Standards

### 1. Naming Conventions

```java
// Classes: PascalCase
public class FileProcessingService { }
public class IrnssDataRepository { }
public class MaterializedViewScheduler { }

// Methods: camelCase (verbs)
public void processFile() { }
public boolean isValidFile() { }
public List<String> getAvailableMjds() { }
public void refreshMaterializedView() { }

// Variables: camelCase (nouns)
private String fileName;
private int mjdValue;
private LocalDateTime processedAt;
private List<IrnssData> dataRecords;

// Constants: UPPER_SNAKE_CASE
public static final String DEFAULT_ENCODING = "UTF-8";
public static final int MAX_RETRY_ATTEMPTS = 3;
public static final long PROCESSING_TIMEOUT_MS = 30000;

// Package names: lowercase
package com.time.tracealibility.services;
package com.time.tracealibility.repository;
package com.time.tracealibility.dto;

// Interfaces: Descriptive names
public interface FileProcessor { }
public interface DataValidator { }
public interface SchedulerService { }

// Implementation classes: Interface + Impl suffix (when needed)
public class DefaultFileProcessor implements FileProcessor { }
public class DatabaseDataValidator implements DataValidator { }
```

### 2. Class Structure

```java
/**
 * Service for processing IRNSS satellite data files.
 * Handles file discovery, parsing, validation, and database persistence.
 */
@Service
@Slf4j
public class IrnssDataService {
    
    // 1. Constants
    private static final String DEFAULT_ENCODING = "UTF-8";
    private static final int BATCH_SIZE = 100;
    
    // 2. Dependencies (final fields)
    private final IrnssDataRepository dataRepository;
    private final ProcessedFileRepository fileRepository;
    private final FileValidator validator;
    
    // 3. Configuration properties
    @Value("${irnss.parent-folder}")
    private String parentFolder;
    
    @Value("${irnss.processing.batch-size:100}")
    private int batchSize;
    
    // 4. Constructor
    public IrnssDataService(IrnssDataRepository dataRepository,
                           ProcessedFileRepository fileRepository,
                           FileValidator validator) {
        this.dataRepository = dataRepository;
        this.fileRepository = fileRepository;
        this.validator = validator;
    }
    
    // 5. Public methods
    public void processFiles() {
        // Implementation
    }
    
    public List<Integer> getAvailableMjds() {
        // Implementation
    }
    
    // 6. Package-private methods (for testing)
    void processFile(Path filePath) {
        // Implementation
    }
    
    // 7. Private methods
    private boolean isValidFile(Path filePath) {
        // Implementation
    }
    
    private void saveDataRecords(List<IrnssData> records) {
        // Implementation
    }
}
```

### 3. Method Design

```java
public class FileProcessingService {
    
    // Good: Clear method signature with validation
    public ProcessingResult processFile(@NonNull Path filePath, 
                                      @NonNull ProcessingOptions options) {
        // Validate inputs
        validateInputs(filePath, options);
        
        try {
            // Main processing logic
            FileInfo fileInfo = extractFileInfo(filePath);
            List<IrnssData> data = parseFileContent(filePath, fileInfo);
            validateData(data);
            saveData(data);
            
            return ProcessingResult.success(data.size());
            
        } catch (IOException e) {
            log.error("IO error processing file: {}", filePath, e);
            return ProcessingResult.failure("IO error: " + e.getMessage());
        } catch (DataValidationException e) {
            log.warn("Data validation failed for file: {}", filePath, e);
            return ProcessingResult.failure("Validation error: " + e.getMessage());
        }
    }
    
    // Good: Single responsibility methods
    private void validateInputs(Path filePath, ProcessingOptions options) {
        Objects.requireNonNull(filePath, "File path cannot be null");
        Objects.requireNonNull(options, "Processing options cannot be null");
        
        if (!Files.exists(filePath)) {
            throw new IllegalArgumentException("File does not exist: " + filePath);
        }
    }
    
    // Good: Early return pattern
    private boolean shouldProcessFile(Path filePath) {
        if (!Files.exists(filePath)) {
            return false;
        }
        
        if (isAlreadyProcessed(filePath)) {
            log.debug("File already processed: {}", filePath);
            return false;
        }
        
        if (!isValidFileType(filePath)) {
            log.debug("Invalid file type: {}", filePath);
            return false;
        }
        
        return true;
    }
}
```

### 4. Exception Handling

```java
public class DataProcessingService {
    
    // Custom exceptions for domain-specific errors
    public static class DataProcessingException extends RuntimeException {
        public DataProcessingException(String message) {
            super(message);
        }
        
        public DataProcessingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    
    // Proper exception handling with context
    public void processFile(Path filePath) {
        try {
            log.info("Starting to process file: {}", filePath);
            
            // Process file
            FileContent content = readFile(filePath);
            List<IrnssData> data = parseContent(content);
            saveData(data);
            
            log.info("Successfully processed file: {} ({} records)", 
                    filePath, data.size());
            
        } catch (IOException e) {
            // Log with context and rethrow as domain exception
            log.error("Failed to read file: {}", filePath, e);
            throw new DataProcessingException(
                "Unable to read file: " + filePath, e);
                
        } catch (ParseException e) {
            // Handle parsing errors specifically
            log.error("Failed to parse file content: {}", filePath, e);
            throw new DataProcessingException(
                "Invalid file format: " + filePath, e);
                
        } catch (Exception e) {
            // Catch-all for unexpected errors
            log.error("Unexpected error processing file: {}", filePath, e);
            throw new DataProcessingException(
                "Processing failed: " + filePath, e);
        }
    }
    
    // Resource management with try-with-resources
    private String readFileContent(Path filePath) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8);
             StringWriter writer = new StringWriter()) {
            
            reader.transferTo(writer);
            return writer.toString();
        }
    }
}
```

### 5. Null Safety and Optional Usage

```java
public class DataService {
    
    // Use Optional for methods that might not return a value
    public Optional<IrnssData> findDataByFileAndLine(String fileName, int lineNumber) {
        IrnssData data = repository.findByFileNameAndLineNumber(fileName, lineNumber);
        return Optional.ofNullable(data);
    }
    
    // Use @NonNull and @Nullable annotations
    public ProcessingResult processData(@NonNull String fileName, 
                                      @Nullable ProcessingOptions options) {
        // Use Optional.ofNullable for nullable parameters
        ProcessingOptions actualOptions = Optional.ofNullable(options)
            .orElse(ProcessingOptions.defaults());
        
        return processWithOptions(fileName, actualOptions);
    }
    
    // Null-safe operations
    public String extractSource(@Nullable String fileName) {
        return Optional.ofNullable(fileName)
            .filter(name -> name.length() >= 6)
            .map(name -> name.substring(0, 6))
            .orElse("UNKNOWN");
    }
    
    // Defensive programming
    public void saveData(@NonNull List<IrnssData> dataList) {
        Objects.requireNonNull(dataList, "Data list cannot be null");
        
        if (dataList.isEmpty()) {
            log.debug("No data to save");
            return;
        }
        
        try {
            repository.saveAll(dataList);
            log.info("Saved {} data records", dataList.size());
        } catch (Exception e) {
            log.error("Failed to save data records", e);
            throw new DataPersistenceException("Failed to save data", e);
        }
    }
}
```

## 🍃 Spring Boot Conventions

### 1. Component Organization

```java
// Controllers - HTTP layer
@RestController
@RequestMapping("/api/data")
@Validated
@Slf4j
public class DataViewController {
    
    private final DataService dataService;
    
    public DataViewController(DataService dataService) {
        this.dataService = dataService;
    }
    
    @GetMapping("/available-mjds")
    public ResponseEntity<ApiResponse<List<Integer>>> getAvailableMjds() {
        try {
            List<Integer> mjds = dataService.getAvailableMjds();
            return ResponseEntity.ok(ApiResponse.success(mjds));
        } catch (Exception e) {
            log.error("Error retrieving available MJDs", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve MJDs"));
        }
    }
}

// Services - Business logic layer
@Service
@Transactional(readOnly = true)
@Slf4j
public class DataService {
    
    private final IrnssDataRepository repository;
    
    public DataService(IrnssDataRepository repository) {
        this.repository = repository;
    }
    
    @Cacheable("available-mjds")
    public List<Integer> getAvailableMjds() {
        return repository.findDistinctMjds();
    }
    
    @Transactional
    public void saveData(List<IrnssData> data) {
        repository.saveAll(data);
    }
}

// Repositories - Data access layer
@Repository
public interface IrnssDataRepository extends JpaRepository<IrnssData, Long> {
    
    @Query("SELECT DISTINCT i.mjd FROM IrnssData i ORDER BY i.mjd DESC")
    List<Integer> findDistinctMjds();
    
    @Query("SELECT i FROM IrnssData i WHERE i.mjd = :mjd AND i.source = :source")
    List<IrnssData> findByMjdAndSource(@Param("mjd") Integer mjd, 
                                      @Param("source") String source);
}
```

### 2. Configuration Classes

```java
@Configuration
@EnableConfigurationProperties({
    IrnssConfigurationProperties.class,
    SchedulerConfigurationProperties.class
})
@Slf4j
public class ApplicationConfiguration {
    
    @Bean
    @ConditionalOnProperty(name = "irnss.scheduler.enabled", havingValue = "true")
    public TaskScheduler fileProcessingScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(5);
        scheduler.setThreadNamePrefix("file-processing-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(30);
        return scheduler;
    }
    
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .registerModule(new JavaTimeModule());
    }
    
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(10, TimeUnit.MINUTES));
        return cacheManager;
    }
}
```

### 3. Configuration Properties

```java
@ConfigurationProperties(prefix = "irnss")
@Data
@Validated
public class IrnssConfigurationProperties {
    
    /**
     * Parent folder containing satellite data files
     */
    @NotBlank(message = "Parent folder must be specified")
    private String parentFolder;
    
    /**
     * Processing configuration
     */
    private Processing processing = new Processing();
    
    /**
     * Scheduler configuration
     */
    private Scheduler scheduler = new Scheduler();
    
    @Data
    public static class Processing {
        /**
         * Number of files to process in each batch
         */
        @Positive
        @Max(10000)
        private int batchSize = 100;
        
        /**
         * Maximum number of retry attempts for failed files
         */
        @Positive
        @Max(10)
        private int maxRetries = 3;
        
        /**
         * Enable parallel processing of directories
         */
        private boolean parallelDirectories = true;
    }
    
    @Data
    public static class Scheduler {
        /**
         * Enable the file processing scheduler
         */
        private boolean enabled = true;
        
        /**
         * Fixed rate for scheduler execution in milliseconds
         */
        @Positive
        private long fixedRate = 300000; // 5 minutes
        
        /**
         * Initial delay before first execution in milliseconds
         */
        @PositiveOrZero
        private long initialDelay = 60000; // 1 minute
    }
}
```

### 4. Error Handling

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidation(ValidationException ex) {
        log.warn("Validation error: {}", ex.getMessage());
        return ResponseEntity.badRequest()
            .body(ApiResponse.error("Validation failed: " + ex.getMessage()));
    }
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNotFound(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return ResponseEntity.notFound().build();
    }
    
    @ExceptionHandler(DataProcessingException.class)
    public ResponseEntity<ApiResponse<Object>> handleDataProcessing(DataProcessingException ex) {
        log.error("Data processing error", ex);
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(ApiResponse.error("Data processing failed: " + ex.getMessage()));
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage()));
        
        return ResponseEntity.badRequest()
            .body(ApiResponse.error("Validation failed", errors));
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGeneral(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("An unexpected error occurred"));
    }
}
```

## 🗄️ Database Standards

### 1. Entity Design

```java
@Entity
@Table(name = "irnss_data", indexes = {
    @Index(name = "idx_irnss_data_mjd_source", columnList = "mjd, source"),
    @Index(name = "idx_irnss_data_file_name", columnList = "file_name"),
    @Index(name = "idx_irnss_data_processed_at", columnList = "processed_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IrnssData {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "source", nullable = false, length = 10)
    private String source;
    
    @Column(name = "mjd", nullable = false)
    private Integer mjd;
    
    @Column(name = "observation_value", nullable = false, precision = 15, scale = 6)
    private Double observationValue;
    
    @Column(name = "secondary_value", precision = 15, scale = 6)
    private Double secondaryValue;
    
    @Column(name = "tertiary_value", precision = 15, scale = 6)
    private Double tertiaryValue;
    
    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;
    
    @Column(name = "line_number", nullable = false)
    private Integer lineNumber;
    
    @Column(name = "processed_at", nullable = false)
    private LocalDateTime processedAt;
    
    @PrePersist
    protected void onCreate() {
        if (processedAt == null) {
            processedAt = LocalDateTime.now();
        }
    }
}
```

### 2. Repository Patterns

```java
@Repository
public interface IrnssDataRepository extends JpaRepository<IrnssData, Long>, 
        JpaSpecificationExecutor<IrnssData> {
    
    // Simple queries using method names
    List<IrnssData> findByMjdAndSource(Integer mjd, String source);
    
    List<IrnssData> findByFileNameOrderByLineNumber(String fileName);
    
    @Query("SELECT DISTINCT i.mjd FROM IrnssData i ORDER BY i.mjd DESC")
    List<Integer> findDistinctMjds();
    
    // Complex queries using @Query
    @Query("""
        SELECT i FROM IrnssData i 
        WHERE i.mjd BETWEEN :startMjd AND :endMjd 
        AND i.source = :source 
        ORDER BY i.mjd, i.lineNumber
        """)
    List<IrnssData> findDataInMjdRange(@Param("startMjd") Integer startMjd,
                                      @Param("endMjd") Integer endMjd,
                                      @Param("source") String source);
    
    // Native queries for performance-critical operations
    @Query(value = """
        SELECT source, mjd, COUNT(*) as record_count,
               AVG(observation_value) as avg_value
        FROM irnss_data 
        WHERE processed_at >= :since
        GROUP BY source, mjd
        ORDER BY mjd DESC
        """, nativeQuery = true)
    List<Object[]> findStatisticsSince(@Param("since") LocalDateTime since);
    
    // Modifying queries
    @Modifying
    @Query("DELETE FROM IrnssData i WHERE i.processedAt < :cutoffDate")
    int deleteOldRecords(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    // Specification for dynamic queries
    static Specification<IrnssData> hasSource(String source) {
        return (root, query, cb) -> 
            source == null ? null : cb.equal(root.get("source"), source);
    }
    
    static Specification<IrnssData> hasMjdBetween(Integer startMjd, Integer endMjd) {
        return (root, query, cb) -> {
            if (startMjd == null && endMjd == null) return null;
            if (startMjd == null) return cb.lessThanOrEqualTo(root.get("mjd"), endMjd);
            if (endMjd == null) return cb.greaterThanOrEqualTo(root.get("mjd"), startMjd);
            return cb.between(root.get("mjd"), startMjd, endMjd);
        };
    }
}
```

### 3. Database Migration Scripts

```sql
-- V1__Create_initial_tables.sql
CREATE TABLE irnss_data (
    id BIGSERIAL PRIMARY KEY,
    source VARCHAR(10) NOT NULL,
    mjd INTEGER NOT NULL,
    observation_value DECIMAL(15,6) NOT NULL,
    secondary_value DECIMAL(15,6),
    tertiary_value DECIMAL(15,6),
    file_name VARCHAR(255) NOT NULL,
    line_number INTEGER NOT NULL,
    processed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_irnss_data_mjd_source ON irnss_data(mjd, source);
CREATE INDEX idx_irnss_data_file_name ON irnss_data(file_name);
CREATE INDEX idx_irnss_data_processed_at ON irnss_data(processed_at);

CREATE TABLE processed_file (
    id BIGSERIAL PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL UNIQUE,
    file_path VARCHAR(500) NOT NULL,
    processed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    file_size BIGINT,
    record_count INTEGER,
    processing_status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',
    error_message TEXT
);

CREATE INDEX idx_processed_file_status ON processed_file(processing_status);
CREATE INDEX idx_processed_file_processed_at ON processed_file(processed_at);
```

## 🧪 Testing Standards

### 1. Unit Test Structure

```java
@ExtendWith(MockitoExtension.class)
class IrnssDataServiceTest {
    
    @Mock
    private IrnssDataRepository dataRepository;
    
    @Mock
    private ProcessedFileRepository fileRepository;
    
    @Mock
    private FileValidator validator;
    
    @InjectMocks
    private IrnssDataService dataService;
    
    @Nested
    @DisplayName("File Processing Tests")
    class FileProcessingTests {
        
        @Test
        @DisplayName("Should process valid file successfully")
        void shouldProcessValidFileSuccessfully() {
            // Given
            Path validFile = Paths.get("test-data/GZLI2P60.866");
            when(validator.isValid(validFile)).thenReturn(true);
            when(fileRepository.existsByFileName("GZLI2P60.866")).thenReturn(false);
            
            // When
            ProcessingResult result = dataService.processFile(validFile);
            
            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getRecordsProcessed()).isGreaterThan(0);
            
            verify(dataRepository).saveAll(any());
            verify(fileRepository).save(any(ProcessedFile.class));
        }
        
        @Test
        @DisplayName("Should skip already processed file")
        void shouldSkipAlreadyProcessedFile() {
            // Given
            Path processedFile = Paths.get("test-data/GZLI2P60.866");
            when(fileRepository.existsByFileName("GZLI2P60.866")).thenReturn(true);
            
            // When
            ProcessingResult result = dataService.processFile(processedFile);
            
            // Then
            assertThat(result.isSkipped()).isTrue();
            assertThat(result.getReason()).contains("already processed");
            
            verify(dataRepository, never()).saveAll(any());
        }
        
        @Test
        @DisplayName("Should handle invalid file gracefully")
        void shouldHandleInvalidFileGracefully() {
            // Given
            Path invalidFile = Paths.get("test-data/invalid-file.txt");
            when(validator.isValid(invalidFile)).thenReturn(false);
            
            // When & Then
            assertThatThrownBy(() -> dataService.processFile(invalidFile))
                .isInstanceOf(DataProcessingException.class)
                .hasMessageContaining("Invalid file");
        }
    }
    
    @Nested
    @DisplayName("MJD Extraction Tests")
    class MjdExtractionTests {
        
        @ParameterizedTest(name = "Should extract MJD {1} from filename {0}")
        @CsvSource({
            "GZLI2P60.866, 60866",
            "GZXY3P60.867, 60867",
            "GZABC160.868, 160868",
            "TEST1P5.123, 5123"
        })
        void shouldExtractMjdFromFilename(String fileName, int expectedMjd) {
            // When
            int actualMjd = dataService.extractMjdFromFileName(fileName);
            
            // Then
            assertThat(actualMjd).isEqualTo(expectedMjd);
        }
        
        @Test
        @DisplayName("Should handle invalid filename format")
        void shouldHandleInvalidFilenameFormat() {
            // Given
            String invalidFileName = "invalid-format.txt";
            
            // When & Then
            assertThatThrownBy(() -> dataService.extractMjdFromFileName(invalidFileName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid filename format");
        }
    }
}
```

### 2. Integration Test Structure

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
class IrnssDataServiceIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:13")
            .withDatabaseName("test_db")
            .withUsername("test_user")
            .withPassword("test_password");
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
    
    @Autowired
    private IrnssDataService dataService;
    
    @Autowired
    private IrnssDataRepository dataRepository;
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    @DisplayName("Should process real satellite data file end-to-end")
    void shouldProcessRealSatelliteDataFileEndToEnd() {
        // Given
        Path testFile = createTestDataFile();
        
        // When
        dataService.processFile(testFile);
        
        // Then
        List<IrnssData> savedData = dataRepository.findByFileName(testFile.getFileName().toString());
        assertThat(savedData).hasSize(2);
        assertThat(savedData.get(0).getSource()).isEqualTo("GZLI2P");
        assertThat(savedData.get(0).getMjd()).isEqualTo(60866);
    }
    
    @Test
    @DisplayName("Should retrieve available MJDs via API")
    void shouldRetrieveAvailableMjdsViaApi() {
        // Given
        seedTestData();
        
        // When
        ResponseEntity<String> response = restTemplate.getForEntity(
            "/api/data/available-mjds", String.class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("60866", "60867");
    }
    
    private Path createTestDataFile() {
        try {
            Path tempFile = Files.createTempFile("GZLI2P60", ".866");
            Files.write(tempFile, Arrays.asList(
                "1 123456.789 123456.790 123456.791",
                "2 123457.789 123457.790 123457.791"
            ));
            return tempFile;
        } catch (IOException e) {
            throw new RuntimeException("Failed to create test file", e);
        }
    }
}
```

### 3. Test Data Management

```java
@TestConfiguration
public class TestDataConfiguration {
    
    @Bean
    @Primary
    public TestDataBuilder testDataBuilder() {
        return new TestDataBuilder();
    }
    
    public static class TestDataBuilder {
        
        public IrnssData buildIrnssData() {
            return IrnssData.builder()
                .source("GZLI2P")
                .mjd(60866)
                .observationValue(123456.789)
                .secondaryValue(123456.790)
                .tertiaryValue(123456.791)
                .fileName("GZLI2P60.866")
                .lineNumber(1)
                .processedAt(LocalDateTime.now())
                .build();
        }
        
        public ProcessedFile buildProcessedFile() {
            return ProcessedFile.builder()
                .fileName("GZLI2P60.866")
                .filePath("/test/data/GZLI2P60.866")
                .processedAt(LocalDateTime.now())
                .fileSize(1024L)
                .recordCount(10)
                .processingStatus(ProcessingStatus.COMPLETED)
                .build();
        }
    }
}
```

## 📚 Documentation Standards

### 1. Class and Method Documentation

```java
/**
 * Service responsible for processing IRNSS (Indian Regional Navigation Satellite System) 
 * satellite data files. Handles file discovery, parsing, validation, and persistence
 * to the database.
 * 
 * <p>The service supports multiple file formats and performs the following operations:
 * <ul>
 *   <li>Discovers new files in configured directories</li>
 *   <li>Extracts source and MJD information from filenames</li>
 *   <li>Parses file content with multiple encoding support</li>
 *   <li>Validates parsed data against business rules</li>
 *   <li>Persists data to PostgreSQL database</li>
 * </ul>
 * 
 * @author Development Team
 * @since 1.0.0
 * @see IrnssData
 * @see ProcessedFile
 */
@Service
public class IrnssDataService {
    
    /**
     * Processes a single satellite data file, extracting and persisting all valid data records.
     * 
     * <p>The method performs the following steps:
     * <ol>
     *   <li>Validates the file exists and is readable</li>
     *   <li>Checks if the file has already been processed</li>
     *   <li>Extracts file metadata (source, MJD) from filename</li>
     *   <li>Parses file content with fallback character encoding</li>
     *   <li>Validates each data record against business rules</li>
     *   <li>Persists valid records to database in batches</li>
     *   <li>Records processing status and statistics</li>
     * </ol>
     * 
     * @param filePath the path to the satellite data file to process
     * @return processing result containing success status and statistics
     * @throws DataProcessingException if file processing fails due to validation or IO errors
     * @throws IllegalArgumentException if filePath is null or file doesn't exist
     * 
     * @since 1.0.0
     */
    public ProcessingResult processFile(@NonNull Path filePath) {
        // Implementation
    }
    
    /**
     * Extracts the Modified Julian Date (MJD) from a satellite data filename.
     * 
     * <p>The MJD is extracted by concatenating all digits found after the 6th character
     * position until the first dot. For example:
     * <ul>
     *   <li>"GZLI2P60.866" → 60866</li>
     *   <li>"GZXY3P60.867" → 60867</li>
     *   <li>"TEST1P123.456" → 123456</li>
     * </ul>
     * 
     * @param fileName the satellite data filename (e.g., "GZLI2P60.866")
     * @return the extracted MJD value
     * @throws IllegalArgumentException if filename format is invalid or MJD cannot be extracted
     * 
     * @since 1.0.0
     */
    int extractMjdFromFileName(@NonNull String fileName) {
        // Implementation
    }
}
```

### 2. Configuration Documentation

```java
/**
 * Configuration properties for IRNSS satellite data processing.
 * 
 * <p>Example configuration in application.properties:
 * <pre>
 * # Basic configuration
 * irnss.parent-folder=/opt/satellite/data
 * irnss.processing.batch-size=100
 * irnss.processing.max-retries=3
 * 
 * # Scheduler configuration
 * irnss.scheduler.enabled=true
 * irnss.scheduler.fixed-rate=300000
 * irnss.scheduler.initial-delay=60000
 * </pre>
 * 
 * @see IrnssDataService
 * @see FileProcessingScheduler
 */
@ConfigurationProperties(prefix = "irnss")
@Data
@Validated
public class IrnssConfigurationProperties {
    
    /**
     * Parent directory containing satellite data files organized by location.
     * 
     * <p>Expected directory structure:
     * <pre>
     * /opt/satellite/data/
     * ├── IRLMF/
     * │   ├── GZLI2P60.866
     * │   └── GZLI2P60.867
     * ├── IRXYZ/
     * │   └── GZXY3P60.866
     * └── IRABC/
     *     └── GZABC60.866
     * </pre>
     * 
     * @since 1.0.0
     */
    @NotBlank(message = "Parent folder must be specified")
    private String parentFolder;
}
```

## 🔒 Security Standards

### 1. Input Validation

```java
@Service
@Validated
public class FileValidationService {
    
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".24d", ".nav", ".obs");
    private static final long MAX_FILE_SIZE_BYTES = 100 * 1024 * 1024; // 100 MB
    private static final Pattern SAFE_FILENAME_PATTERN = Pattern.compile("^[a-zA-Z0-9._-]+$");
    
    /**
     * Validates that the file is safe to process.
     */
    public void validateFile(@NonNull Path filePath) {
        String fileName = filePath.getFileName().toString();
        
        // Validate filename characters
        if (!SAFE_FILENAME_PATTERN.matcher(fileName).matches()) {
            throw new SecurityException("Unsafe characters in filename: " + fileName);
        }
        
        // Validate file extension
        String extension = getFileExtension(fileName);
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new ValidationException("Invalid file extension: " + extension);
        }
        
        // Validate file size
        try {
            long fileSize = Files.size(filePath);
            if (fileSize > MAX_FILE_SIZE_BYTES) {
                throw new ValidationException("File too large: " + fileSize + " bytes");
            }
        } catch (IOException e) {
            throw new ValidationException("Cannot read file size", e);
        }
        
        // Validate path traversal
        validatePathTraversal(filePath);
    }
    
    /**
     * Prevents directory traversal attacks.
     */
    private void validatePathTraversal(Path filePath) {
        try {
            Path normalizedPath = filePath.normalize();
            if (!normalizedPath.equals(filePath)) {
                throw new SecurityException("Path traversal attempt detected: " + filePath);
            }
        } catch (Exception e) {
            throw new SecurityException("Invalid file path: " + filePath, e);
        }
    }
    
    /**
     * Sanitizes user input to prevent injection attacks.
     */
    public String sanitizeInput(@Nullable String input) {
        if (input == null) {
            return null;
        }
        
        return input.replaceAll("[<>\"'%;()&+]", "")
                   .trim()
                   .substring(0, Math.min(input.length(), 255));
    }
}
```

### 2. Error Information Leakage Prevention

```java
@RestControllerAdvice
public class SecurityAwareExceptionHandler {
    
    private static final Logger log = LoggerFactory.getLogger(SecurityAwareExceptionHandler.class);
    
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDenied(AccessDeniedException ex) {
        // Log full error details server-side
        log.error("Access denied", ex);
        
        // Return generic message to client
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ApiResponse.error("Access denied"));
    }
    
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ApiResponse<Object>> handleSecurity(SecurityException ex) {
        // Log full error details server-side
        log.error("Security violation", ex);
        
        // Return generic message to client
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error("Invalid request"));
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGeneral(Exception ex) {
        // Log full error with unique ID
        String errorId = UUID.randomUUID().toString();
        log.error("Unexpected error [{}]", errorId, ex);
        
        // Return generic message with error ID for support
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("An error occurred. Reference: " + errorId));
    }
}
```

## ⚡ Performance Standards

### 1. Efficient Data Processing

```java
@Service
public class HighPerformanceDataService {
    
    private static final int BATCH_SIZE = 1000;
    private static final int PARALLEL_THRESHOLD = 10000;
    
    /**
     * Processes large datasets efficiently using batch operations and parallel processing.
     */
    @Transactional
    public void processLargeDataset(List<IrnssData> dataList) {
        if (dataList.size() > PARALLEL_THRESHOLD) {
            processInParallel(dataList);
        } else {
            processInBatches(dataList);
        }
    }
    
    private void processInBatches(List<IrnssData> dataList) {
        for (int i = 0; i < dataList.size(); i += BATCH_SIZE) {
            int endIndex = Math.min(i + BATCH_SIZE, dataList.size());
            List<IrnssData> batch = dataList.subList(i, endIndex);
            
            repository.saveAll(batch);
            
            // Clear persistence context to free memory
            if (i % (BATCH_SIZE * 10) == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }
    }
    
    private void processInParallel(List<IrnssData> dataList) {
        // Partition data for parallel processing
        List<List<IrnssData>> partitions = partitionList(dataList, BATCH_SIZE);
        
        // Process partitions in parallel
        partitions.parallelStream()
            .forEach(this::processBatch);
    }
    
    /**
     * Uses database-specific optimizations for bulk operations.
     */
    @Transactional
    public void bulkInsert(List<IrnssData> dataList) {
        // Use JDBC batch insert for better performance
        String sql = """
            INSERT INTO irnss_data (source, mjd, observation_value, file_name, line_number, processed_at)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        
        jdbcTemplate.batchUpdate(sql, dataList, BATCH_SIZE,
            (PreparedStatement ps, IrnssData data) -> {
                ps.setString(1, data.getSource());
                ps.setInt(2, data.getMjd());
                ps.setDouble(3, data.getObservationValue());
                ps.setString(4, data.getFileName());
                ps.setInt(5, data.getLineNumber());
                ps.setTimestamp(6, Timestamp.valueOf(data.getProcessedAt()));
            });
    }
}
```

### 2. Caching Strategies

```java
@Service
@CacheConfig(cacheNames = "data-cache")
public class CachedDataService {
    
    /**
     * Caches frequently accessed MJD data with 10-minute expiration.
     */
    @Cacheable(key = "'available-mjds'", unless = "#result.isEmpty()")
    public List<Integer> getAvailableMjds() {
        return repository.findDistinctMjds();
    }
    
    /**
     * Caches file availability data with dynamic key.
     */
    @Cacheable(key = "'file-availability-' + #mjd + '-' + (#source ?: 'all')")
    public List<FileAvailability> getFileAvailability(Integer mjd, String source) {
        return repository.findFileAvailability(mjd, source);
    }
    
    /**
     * Evicts cache when data is updated.
     */
    @CacheEvict(allEntries = true)
    public void refreshCache() {
        log.info("Cache evicted for data updates");
    }
    
    /**
     * Custom cache configuration for different data types.
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        
        // Configure different expiration policies
        Map<String, CaffeineSpec> cacheSpecs = Map.of(
            "data-cache", CaffeineSpec.parse("maximumSize=1000,expireAfterWrite=10m"),
            "statistics-cache", CaffeineSpec.parse("maximumSize=100,expireAfterWrite=5m"),
            "config-cache", CaffeineSpec.parse("maximumSize=50,expireAfterWrite=1h")
        );
        
        cacheManager.setCaffeineSpecs(cacheSpecs);
        return cacheManager;
    }
}
```

## 👀 Code Review Standards

### 1. Review Checklist

```markdown
## Code Review Checklist

### Functionality
- [ ] Code works as intended and meets requirements
- [ ] Edge cases are handled appropriately
- [ ] Error handling is comprehensive and appropriate
- [ ] Business logic is correct and efficient

### Code Quality
- [ ] Code is readable and well-structured
- [ ] Methods are focused and have single responsibility
- [ ] Variable and method names are descriptive
- [ ] Code follows established patterns and conventions

### Testing
- [ ] Unit tests cover new/modified functionality
- [ ] Integration tests verify end-to-end behavior
- [ ] Test cases include edge cases and error scenarios
- [ ] Test coverage meets project standards (>80%)

### Performance
- [ ] No obvious performance bottlenecks
- [ ] Database queries are optimized
- [ ] Memory usage is reasonable
- [ ] Caching is used appropriately

### Security
- [ ] Input validation is implemented
- [ ] No sensitive data is logged or exposed
- [ ] Authentication/authorization is correct
- [ ] No potential injection vulnerabilities

### Documentation
- [ ] Public APIs are properly documented
- [ ] Complex logic is explained with comments
- [ ] Configuration changes are documented
- [ ] README/wiki updates are included if needed
```

### 2. Review Comments Guidelines

```java
// ✅ Good review comments:

// "Consider using Optional.ofNullable() here to handle potential null values"
public String extractSource(String fileName) {
    return fileName.substring(0, 6); // This could throw StringIndexOutOfBoundsException
}

// "This method is doing too much. Consider extracting file validation into a separate method"
public void processFile(Path filePath) {
    // 50 lines of mixed validation and processing logic
}

// "Great use of the builder pattern! This makes the code much more readable"
IrnssData data = IrnssData.builder()
    .source(source)
    .mjd(mjd)
    .build();

// ❌ Avoid these types of comments:
// "This is wrong" - Not helpful, explain what's wrong and suggest a solution
// "Change this" - Too vague, be specific about what needs to change
// "I don't like this" - Focus on technical issues, not personal preferences
```

## 🎯 Quality Assurance

### 1. Static Code Analysis

```xml
<!-- pom.xml - Quality analysis plugins -->
<plugin>
    <groupId>com.github.spotbugs</groupId>
    <artifactId>spotbugs-maven-plugin</artifactId>
    <version>4.7.3.0</version>
    <configuration>
        <effort>Max</effort>
        <threshold>Low</threshold>
        <failOnError>true</failOnError>
    </configuration>
</plugin>

<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.8</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
        <execution>
            <id>check</id>
            <goals>
                <goal>check</goal>
            </goals>
            <configuration>
                <rules>
                    <rule>
                        <element>BUNDLE</element>
                        <limits>
                            <limit>
                                <counter>INSTRUCTION</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.80</minimum>
                            </limit>
                        </limits>
                    </rule>
                </rules>
            </configuration>
        </execution>
    </executions>
</plugin>
```

### 2. Code Metrics and Quality Gates

```java
// Quality metrics to monitor:
// - Cyclomatic Complexity: < 10 per method
// - Test Coverage: > 80%
// - Code Duplication: < 5%
// - Technical Debt Ratio: < 5%

@Component
public class CodeQualityMetrics {
    
    // Good: Simple method with low complexity
    public boolean isValidFileExtension(String fileName) {
        String extension = getFileExtension(fileName).toLowerCase();
        return ALLOWED_EXTENSIONS.contains(extension);
    }
    
    // Refactor: Method with high complexity - should be broken down
    public ProcessingResult processFileWithValidation(Path filePath) {
        // This method has cyclomatic complexity > 10
        // Should be refactored into smaller methods
        if (filePath == null) return ProcessingResult.failure("Null path");
        if (!Files.exists(filePath)) return ProcessingResult.failure("File not found");
        if (!isValidExtension(filePath)) return ProcessingResult.failure("Invalid extension");
        // ... many more conditions
        
        // Better approach: Extract validation methods
        return validateAndProcess(filePath);
    }
    
    private ProcessingResult validateAndProcess(Path filePath) {
        ValidationResult validation = validator.validate(filePath);
        if (!validation.isValid()) {
            return ProcessingResult.failure(validation.getErrorMessage());
        }
        
        return processor.process(filePath);
    }
}
```

---

## 📞 Code Standards Support

For code standards questions and improvements:

1. **Style Questions**: Refer to this guide and team coding standards
2. **Best Practices**: Consult with senior developers and architects
3. **Tool Configuration**: Check IDE setup and build tool configurations
4. **Quality Issues**: Review static analysis reports and metrics

**Related Documentation**: 
- [Development Workflow](Development-Workflow.md) for development processes
- [Testing Strategy](Testing-Strategy.md) for testing standards
- [Environment Setup](Environment-Setup.md) for tool configuration

---

**Code Standards Complete!** 📏 Your codebase maintains high quality with consistent patterns and best practices across all components.
