# 🔗 API Design

Comprehensive REST API architecture and design patterns for the Time Traceability Backend system, covering endpoints, data models, error handling, and best practices.

## 📋 Table of Contents

- [API Architecture Overview](#-api-architecture-overview)
- [RESTful Design Principles](#-restful-design-principles)
- [API Endpoints](#-api-endpoints)
- [Request/Response Patterns](#-requestresponse-patterns)
- [Error Handling](#-error-handling)
- [Data Transfer Objects (DTOs)](#-data-transfer-objects-dtos)
- [API Versioning](#-api-versioning)
- [Security Patterns](#-security-patterns)
- [Performance Considerations](#-performance-considerations)
- [API Documentation](#-api-documentation)

## 🏗️ API Architecture Overview

The Time Traceability Backend follows a layered REST API architecture:

```
┌─────────────────────────────────────────────────────────────┐
│                        Frontend/Client                      │
└─────────────────────────┬───────────────────────────────────┘
                          │ HTTP/REST
┌─────────────────────────▼───────────────────────────────────┐
│                    API Gateway/Load Balancer                │
└─────────────────────────┬───────────────────────────────────┘
                          │
┌─────────────────────────▼───────────────────────────────────┐
│                   Spring Boot Application                   │
│  ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐ │
│  │   Controllers   │ │   CORS Config   │ │  Error Handler  │ │
│  └─────────┬───────┘ └─────────────────┘ └─────────────────┘ │
│            │                                                 │
│  ┌─────────▼───────┐ ┌─────────────────┐ ┌─────────────────┐ │
│  │    Services     │ │      DTOs       │ │   Validation    │ │
│  └─────────┬───────┘ └─────────────────┘ └─────────────────┘ │
│            │                                                 │
│  ┌─────────▼───────┐ ┌─────────────────┐ ┌─────────────────┐ │
│  │  Repositories   │ │    Entities     │ │   Scheduling    │ │
│  └─────────┬───────┘ └─────────────────┘ └─────────────────┘ │
└────────────┼─────────────────────────────────────────────────┘
             │
┌────────────▼─────────────────────────────────────────────────┐
│                    PostgreSQL Database                      │
│         Tables, Views, Materialized Views                   │
└──────────────────────────────────────────────────────────────┘
```

### Core API Principles

1. **RESTful Design**: HTTP methods aligned with CRUD operations
2. **Resource-Based URLs**: Clear, hierarchical resource naming
3. **Stateless**: Each request contains all necessary information
4. **JSON**: Primary data exchange format
5. **HTTP Status Codes**: Meaningful response codes
6. **Consistent Patterns**: Uniform structure across endpoints

## 📐 RESTful Design Principles

### 1. Resource Naming Conventions

```
✅ Good Resource Names:
GET    /api/data/available-mjds           # Get available MJD dates
GET    /api/data/file-availability        # Get file availability data
GET    /api/statistics/file-upload-stats  # Get file upload statistics
GET    /api/scheduler/status              # Get scheduler status
POST   /api/scheduler/start               # Start scheduler
POST   /api/scheduler/stop                # Stop scheduler

❌ Avoid:
GET    /api/getMjds                       # Non-RESTful naming
POST   /api/data/getData                  # Verb in URL
GET    /api/fileAvailability             # Mixed casing
```

### 2. HTTP Method Usage

```java
// GET - Retrieve data (idempotent, safe)
@GetMapping("/api/data/available-mjds")
public ResponseEntity<List<Integer>> getAvailableMjds()

// POST - Create new resources or trigger actions
@PostMapping("/api/scheduler/start")
public ResponseEntity<Map<String, String>> startScheduler()

// PUT - Update entire resource (idempotent)
@PutMapping("/api/data/processing-config")
public ResponseEntity<ProcessingConfig> updateConfig(@RequestBody ProcessingConfig config)

// PATCH - Partial update
@PatchMapping("/api/scheduler/settings")
public ResponseEntity<SchedulerSettings> updateSettings(@RequestBody Map<String, Object> updates)

// DELETE - Remove resource (idempotent)
@DeleteMapping("/api/data/processed-files/{id}")
public ResponseEntity<Void> deleteProcessedFile(@PathVariable Long id)
```

### 3. Status Code Standards

```java
// Success Responses
200 OK          - GET requests with data
201 Created     - POST requests that create resources
202 Accepted    - Async operations started
204 No Content  - Successful DELETE or PUT with no response body

// Client Error Responses
400 Bad Request        - Invalid request format/data
401 Unauthorized       - Authentication required
403 Forbidden          - Access denied
404 Not Found          - Resource doesn't exist
409 Conflict           - Resource conflict
422 Unprocessable      - Validation errors

// Server Error Responses
500 Internal Error     - Unexpected server error
502 Bad Gateway        - Upstream service error
503 Service Unavailable - Temporary unavailability
```

## 🎯 API Endpoints

### Data Endpoints (`DataViewController`)

#### 1. Available MJDs
```http
GET /api/data/available-mjds
```

**Description**: Retrieve list of available Modified Julian Dates

**Response**:
```json
{
  "status": "success",
  "data": [60866, 60867, 60868, 60869],
  "count": 4,
  "timestamp": "2025-09-08T10:30:00Z"
}
```

**Implementation Pattern**:
```java
@GetMapping("/available-mjds")
public ResponseEntity<ApiResponse<List<Integer>>> getAvailableMjds() {
    try {
        List<Integer> mjds = dataService.getAvailableMjds();
        return ResponseEntity.ok(ApiResponse.success(mjds));
    } catch (Exception e) {
        log.error("Error retrieving available MJDs", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("Failed to retrieve available MJDs"));
    }
}
```

#### 2. File Availability Data
```http
GET /api/data/file-availability?mjd={mjd}&source={source}
```

**Parameters**:
- `mjd` (optional): Filter by Modified Julian Date
- `source` (optional): Filter by data source

**Response**:
```json
{
  "status": "success",
  "data": [
    {
      "mjd": 60866,
      "source": "GZLI2P",
      "fileName": "GZLI2P60.866",
      "isAvailable": true,
      "lastUpdated": "2025-09-08T10:30:00Z",
      "fileSize": 1024,
      "recordCount": 100
    }
  ],
  "pagination": {
    "page": 1,
    "size": 20,
    "total": 150,
    "totalPages": 8
  }
}
```

### Statistics Endpoints (`StatisticsController`)

#### 1. File Upload Statistics
```http
GET /api/statistics/file-upload-stats?startDate={date}&endDate={date}
```

**Response**:
```json
{
  "status": "success",
  "data": {
    "summaryStats": {
      "totalFiles": 1500,
      "totalSizeMB": 2048.5,
      "processingRate": 95.2,
      "avgProcessingTimeMs": 1250
    },
    "dailyUploadStats": [
      {
        "date": "2025-09-08",
        "fileCount": 45,
        "totalSizeMB": 67.8,
        "successRate": 97.8
      }
    ],
    "fileTypeStats": [
      {
        "extension": ".24d",
        "count": 800,
        "percentage": 53.3
      }
    ],
    "hourlyUploadPattern": [
      {
        "hour": 0,
        "fileCount": 12,
        "avgSizeMB": 1.2
      }
    ]
  }
}
```

### Scheduler Endpoints (`SchedulerController`)

#### 1. Scheduler Status
```http
GET /api/scheduler/status
```

**Response**:
```json
{
  "status": "success",
  "data": {
    "isRunning": true,
    "lastRunTime": "2025-09-08T10:25:00Z",
    "nextRunTime": "2025-09-08T10:30:00Z",
    "filesProcessed": 1250,
    "currentBatch": 15,
    "processingState": "PROCESSING_IRLMF"
  }
}
```

#### 2. Scheduler Control
```http
POST /api/scheduler/start
POST /api/scheduler/stop
POST /api/scheduler/restart
```

**Response**:
```json
{
  "status": "success",
  "message": "Scheduler started successfully",
  "data": {
    "action": "start",
    "timestamp": "2025-09-08T10:30:00Z",
    "previousState": "stopped",
    "currentState": "running"
  }
}
```

#### 3. Health Check
```http
GET /api/scheduler/health
```

**Response**:
```json
{
  "status": "UP",
  "components": {
    "database": {
      "status": "UP",
      "details": {
        "connectionPool": "healthy",
        "activeConnections": 5,
        "maxConnections": 20
      }
    },
    "fileSystem": {
      "status": "UP",
      "details": {
        "dataFolder": "/opt/satellite/data",
        "freeSpace": "45.2GB",
        "accessible": true
      }
    },
    "scheduler": {
      "status": "UP",
      "details": {
        "running": true,
        "lastRun": "2025-09-08T10:25:00Z"
      }
    }
  }
}
```

## 📤 Request/Response Patterns

### 1. Standard API Response Wrapper

```java
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private String status;      // "success" | "error" | "warning"
    private String message;     // Human-readable message
    private T data;            // Response payload
    private String timestamp;  // ISO 8601 timestamp
    private Map<String, Object> metadata; // Additional metadata
    
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("success", null, data, Instant.now().toString(), null);
    }
    
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>("error", message, null, Instant.now().toString(), null);
    }
    
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>("success", message, data, Instant.now().toString(), null);
    }
}
```

### 2. Pagination Pattern

```java
public class PaginatedResponse<T> {
    private List<T> content;
    private PaginationInfo pagination;
    
    @Data
    public static class PaginationInfo {
        private int page;          // Current page (0-based)
        private int size;          // Page size
        private long total;        // Total number of items
        private int totalPages;    // Total number of pages
        private boolean hasNext;   // Has next page
        private boolean hasPrevious; // Has previous page
    }
}

// Usage in controller
@GetMapping("/file-availability")
public ResponseEntity<ApiResponse<PaginatedResponse<FileAvailability>>> getFileAvailability(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(required = false) Integer mjd,
        @RequestParam(required = false) String source) {
    
    Pageable pageable = PageRequest.of(page, size);
    Page<FileAvailability> pageResult = dataService.getFileAvailability(pageable, mjd, source);
    
    PaginatedResponse<FileAvailability> response = new PaginatedResponse<>();
    response.setContent(pageResult.getContent());
    response.setPagination(PaginationInfo.builder()
        .page(page)
        .size(size)
        .total(pageResult.getTotalElements())
        .totalPages(pageResult.getTotalPages())
        .hasNext(pageResult.hasNext())
        .hasPrevious(pageResult.hasPrevious())
        .build());
    
    return ResponseEntity.ok(ApiResponse.success(response));
}
```

### 3. Filtering and Sorting

```java
// Query parameter patterns
@GetMapping("/data/file-availability")
public ResponseEntity<?> getFileAvailability(
    // Pagination
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int size,
    
    // Sorting
    @RequestParam(defaultValue = "mjd") String sortBy,
    @RequestParam(defaultValue = "desc") String sortDir,
    
    // Filtering
    @RequestParam(required = false) Integer mjd,
    @RequestParam(required = false) String source,
    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
    
    // Search
    @RequestParam(required = false) String search) {
    
    Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
    Pageable pageable = PageRequest.of(page, size, sort);
    
    FileAvailabilityFilter filter = FileAvailabilityFilter.builder()
        .mjd(mjd)
        .source(source)
        .startDate(startDate)
        .endDate(endDate)
        .search(search)
        .build();
    
    Page<FileAvailability> result = dataService.getFileAvailability(pageable, filter);
    return ResponseEntity.ok(ApiResponse.success(result));
}
```

## ⚠️ Error Handling

### 1. Global Exception Handler

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidation(ValidationException ex) {
        log.warn("Validation error: {}", ex.getMessage());
        return ResponseEntity.badRequest()
            .body(ApiResponse.error("Validation failed: " + ex.getMessage()));
    }
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNotFound(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return ResponseEntity.notFound()
            .build();
    }
    
    @ExceptionHandler(DataProcessingException.class)
    public ResponseEntity<ApiResponse<Object>> handleDataProcessing(DataProcessingException ex) {
        log.error("Data processing error", ex);
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(ApiResponse.error("Data processing failed: " + ex.getMessage()));
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGeneral(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("An unexpected error occurred"));
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
}
```

### 2. Custom Exception Classes

```java
// Base exception
public abstract class TimeTraceabilityException extends RuntimeException {
    protected TimeTraceabilityException(String message) {
        super(message);
    }
    
    protected TimeTraceabilityException(String message, Throwable cause) {
        super(message, cause);
    }
}

// Specific exceptions
public class ResourceNotFoundException extends TimeTraceabilityException {
    public ResourceNotFoundException(String resource, Object id) {
        super(String.format("%s not found with id: %s", resource, id));
    }
}

public class DataProcessingException extends TimeTraceabilityException {
    public DataProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}

public class SchedulerException extends TimeTraceabilityException {
    public SchedulerException(String message) {
        super(message);
    }
}
```

### 3. Error Response Format

```json
{
  "status": "error",
  "message": "Validation failed",
  "timestamp": "2025-09-08T10:30:00Z",
  "data": {
    "mjd": "MJD must be a valid number",
    "source": "Source cannot be empty"
  },
  "metadata": {
    "errorCode": "VALIDATION_ERROR",
    "requestId": "req-12345-67890",
    "path": "/api/data/file-availability"
  }
}
```

## 📊 Data Transfer Objects (DTOs)

### 1. File Upload Statistics DTO

```java
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FileUploadStatsDTO {
    
    private SummaryStats summaryStats;
    private List<DailyUploadStats> dailyUploadStats;
    private List<FileTypeStats> fileTypeStats;
    private List<LocationUploadStats> locationUploadStats;
    private List<HourlyUploadPattern> hourlyUploadPattern;
    private List<RecentFileUpload> recentFileUploads;
    private List<FileWithErrors> filesWithErrors;
    private List<MissingFileInfo> missingFiles;
    private ProcessingPerformanceStats processingPerformance;
    
    @Data
    @Builder
    public static class SummaryStats {
        private Long totalFiles;
        private Double totalSizeMB;
        private Double processingSuccessRate;
        private Long avgProcessingTimeMs;
        private Integer uniqueSources;
        private Integer activeDays;
    }
    
    @Data
    @Builder
    public static class DailyUploadStats {
        private LocalDate date;
        private Long fileCount;
        private Double totalSizeMB;
        private Double successRate;
        private Long avgProcessingTimeMs;
    }
    
    @Data
    @Builder
    public static class FileTypeStats {
        private String fileExtension;
        private Long count;
        private Double percentage;
        private Double avgSizeMB;
    }
    
    @Data
    @Builder
    public static class LocationUploadStats {
        private String location;
        private Long fileCount;
        private Double totalSizeMB;
        private Double successRate;
        private LocalDateTime lastUpload;
    }
}
```

### 2. Source Session Status DTO

```java
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SourceSessionStatusDTO {
    
    private String source;
    private Integer mjd;
    private SessionStatus status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long duration; // in milliseconds
    private SessionStats stats;
    private List<String> errors;
    private Map<String, Object> metadata;
    
    public enum SessionStatus {
        PENDING, IN_PROGRESS, COMPLETED, FAILED, CANCELLED
    }
    
    @Data
    @Builder
    public static class SessionStats {
        private Long filesProcessed;
        private Long recordsProcessed;
        private Long bytesProcessed;
        private Double processingRate; // files per minute
        private Long avgFileProcessingTime; // milliseconds
    }
}
```

### 3. API Response DTOs

```java
// Generic API response wrapper
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private String status;
    private String message;
    private T data;
    private String timestamp;
    private Map<String, Object> metadata;
    
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
            .status("success")
            .data(data)
            .timestamp(Instant.now().toString())
            .build();
    }
    
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
            .status("error")
            .message(message)
            .timestamp(Instant.now().toString())
            .build();
    }
}

// Health check response
@Data
@Builder
public class HealthCheckResponse {
    private String status; // UP, DOWN, DEGRADED
    private Map<String, ComponentHealth> components;
    private Map<String, Object> details;
    
    @Data
    @Builder
    public static class ComponentHealth {
        private String status;
        private Map<String, Object> details;
    }
}
```

## 📈 API Versioning

### 1. URL-Based Versioning (Current Approach)

```java
// Version 1 API (current)
@RestController
@RequestMapping("/api/v1/data")
public class DataControllerV1 {
    
    @GetMapping("/available-mjds")
    public ResponseEntity<List<Integer>> getAvailableMjds() {
        // V1 implementation
    }
}

// Version 2 API (future)
@RestController
@RequestMapping("/api/v2/data")
public class DataControllerV2 {
    
    @GetMapping("/available-mjds")
    public ResponseEntity<ApiResponse<List<MjdInfo>>> getAvailableMjds() {
        // V2 implementation with enhanced response format
    }
}
```

### 2. Header-Based Versioning (Alternative)

```java
@RestController
@RequestMapping("/api/data")
public class DataController {
    
    @GetMapping(value = "/available-mjds", headers = "API-Version=1")
    public ResponseEntity<List<Integer>> getAvailableMjdsV1() {
        // V1 implementation
    }
    
    @GetMapping(value = "/available-mjds", headers = "API-Version=2")
    public ResponseEntity<ApiResponse<List<MjdInfo>>> getAvailableMjdsV2() {
        // V2 implementation
    }
}
```

### 3. Version Compatibility Strategy

```java
@Component
public class ApiVersionCompatibility {
    
    public Object adaptResponseForVersion(Object response, String version) {
        switch (version) {
            case "1":
                return adaptToV1(response);
            case "2":
                return adaptToV2(response);
            default:
                return response; // Latest version
        }
    }
    
    private Object adaptToV1(Object response) {
        // Convert V2+ response to V1 format
        if (response instanceof ApiResponse) {
            return ((ApiResponse<?>) response).getData();
        }
        return response;
    }
}
```

## 🔒 Security Patterns

### 1. CORS Configuration

```java
@Configuration
@EnableWebMvc
public class CorsConfig implements WebMvcConfigurer {
    
    @Value("${app.cors.allowed-origins}")
    private String[] allowedOrigins;
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
```

### 2. Input Validation

```java
// DTO validation
@Data
@Builder
public class FileProcessingRequest {
    
    @NotNull(message = "MJD is required")
    @Min(value = 1, message = "MJD must be positive")
    @Max(value = 999999, message = "MJD must be valid")
    private Integer mjd;
    
    @NotBlank(message = "Source is required")
    @Pattern(regexp = "^[A-Z0-9]{1,10}$", message = "Source must be alphanumeric uppercase")
    private String source;
    
    @Valid
    private ProcessingOptions options;
}

// Controller validation
@PostMapping("/process-files")
public ResponseEntity<ApiResponse<Object>> processFiles(
        @Valid @RequestBody FileProcessingRequest request) {
    // Processing logic
}
```

### 3. Rate Limiting (Future Enhancement)

```java
@Component
public class RateLimitingInterceptor implements HandlerInterceptor {
    
    private final RateLimiter rateLimiter = RateLimiter.create(60.0); // 60 requests per minute
    
    @Override
    public boolean preHandle(HttpServletRequest request, 
                           HttpServletResponse response, 
                           Object handler) throws Exception {
        
        if (!rateLimiter.tryAcquire()) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("{\"error\":\"Rate limit exceeded\"}");
            return false;
        }
        
        return true;
    }
}
```

## ⚡ Performance Considerations

### 1. Caching Strategy

```java
@Service
@CacheConfig(cacheNames = "data-cache")
public class DataService {
    
    @Cacheable(key = "#root.methodName")
    public List<Integer> getAvailableMjds() {
        // Expensive database query
        return repository.findDistinctMjds();
    }
    
    @Cacheable(key = "'file-availability-' + #mjd + '-' + #source")
    public List<FileAvailability> getFileAvailability(Integer mjd, String source) {
        // Complex query with materialized views
        return repository.findFileAvailability(mjd, source);
    }
    
    @CacheEvict(allEntries = true)
    public void refreshCache() {
        // Manual cache refresh
    }
}
```

### 2. Async Processing

```java
@RestController
public class AsyncProcessingController {
    
    @Autowired
    private AsyncProcessingService asyncService;
    
    @PostMapping("/api/data/process-async")
    public ResponseEntity<ApiResponse<Object>> processAsync(
            @RequestBody ProcessingRequest request) {
        
        String taskId = UUID.randomUUID().toString();
        asyncService.processAsync(request, taskId);
        
        Map<String, String> response = Map.of(
            "taskId", taskId,
            "status", "STARTED",
            "statusUrl", "/api/data/processing-status/" + taskId
        );
        
        return ResponseEntity.accepted()
            .body(ApiResponse.success(response, "Processing started"));
    }
    
    @GetMapping("/api/data/processing-status/{taskId}")
    public ResponseEntity<ApiResponse<TaskStatus>> getProcessingStatus(
            @PathVariable String taskId) {
        
        TaskStatus status = asyncService.getTaskStatus(taskId);
        return ResponseEntity.ok(ApiResponse.success(status));
    }
}
```

### 3. Database Query Optimization

```java
@Repository
public class OptimizedDataRepository {
    
    // Use native queries for complex operations
    @Query(value = """
        SELECT DISTINCT mjd 
        FROM sat_common_view_difference_materialized 
        WHERE created_at >= :startDate 
        ORDER BY mjd DESC 
        LIMIT :limit
        """, nativeQuery = true)
    List<Integer> findRecentMjds(@Param("startDate") LocalDateTime startDate, 
                                @Param("limit") int limit);
    
    // Use projections for specific fields
    @Query("SELECT f.mjd as mjd, f.source as source, f.fileName as fileName " +
           "FROM FileAvailability f WHERE f.mjd = :mjd")
    List<FileAvailabilityProjection> findFileAvailabilityProjection(@Param("mjd") Integer mjd);
    
    interface FileAvailabilityProjection {
        Integer getMjd();
        String getSource();
        String getFileName();
    }
}
```

## 📚 API Documentation

### 1. OpenAPI/Swagger Configuration

```java
@Configuration
@EnableSwagger2
public class SwaggerConfig {
    
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.time.tracealibility.controllers"))
                .paths(PathSelectors.regex("/api.*"))
                .build()
                .apiInfo(apiInfo())
                .securitySchemes(Arrays.asList(apiKey()))
                .securityContexts(Arrays.asList(securityContext()));
    }
    
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("Time Traceability Backend API")
                .description("REST API for IRNSS satellite data processing and monitoring")
                .version("1.0.0")
                .contact(new Contact("Development Team", "", "dev@example.com"))
                .build();
    }
}
```

### 2. API Documentation Annotations

```java
@RestController
@RequestMapping("/api/data")
@Api(tags = "Data Management", description = "Satellite data retrieval and management")
public class DataViewController {
    
    @GetMapping("/available-mjds")
    @ApiOperation(
        value = "Get available MJD dates",
        notes = "Retrieves a list of all Modified Julian Dates for which satellite data is available",
        response = Integer.class,
        responseContainer = "List"
    )
    @ApiResponses({
        @ApiResponse(code = 200, message = "Successfully retrieved MJD list"),
        @ApiResponse(code = 500, message = "Internal server error")
    })
    public ResponseEntity<ApiResponse<List<Integer>>> getAvailableMjds() {
        // Implementation
    }
    
    @GetMapping("/file-availability")
    @ApiOperation(value = "Get file availability data")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "mjd", value = "Modified Julian Date", dataType = "int", paramType = "query"),
        @ApiImplicitParam(name = "source", value = "Data source", dataType = "string", paramType = "query"),
        @ApiImplicitParam(name = "page", value = "Page number", dataType = "int", paramType = "query", defaultValue = "0"),
        @ApiImplicitParam(name = "size", value = "Page size", dataType = "int", paramType = "query", defaultValue = "20")
    })
    public ResponseEntity<?> getFileAvailability(
        @RequestParam(required = false) Integer mjd,
        @RequestParam(required = false) String source,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {
        // Implementation
    }
}
```

---

## 📞 API Design Support

For API design questions and improvements:

1. **Endpoint Design**: Follow REST principles and resource-based naming
2. **Error Handling**: Use appropriate HTTP status codes and consistent error formats
3. **Performance**: Implement caching, pagination, and async processing where needed
4. **Documentation**: Keep API documentation updated with code changes

**Related Documentation**: 
- [Data Flow](Data-Flow.md) for understanding how data moves through the API
- [Testing Strategy](Testing-Strategy.md) for API testing approaches
- [Security Guide](Security-Guide.md) for API security best practices

---

**API Design Complete!** 🎯 Your REST API follows modern design patterns and best practices for maintainable, scalable satellite data processing.
