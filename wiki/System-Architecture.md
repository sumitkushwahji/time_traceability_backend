# 🏗️ System Architecture

This document provides a comprehensive overview of the Time Traceability Backend system architecture, design patterns, and component interactions.

## 🌐 High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                          External Clients                                   │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐   │
│  │   Frontend   │  │   Mobile     │  │  Analytics   │  │   Monitoring │   │
│  │   Dashboard  │  │     App      │  │   Tools      │  │    Tools     │   │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘   │
└─────────┼──────────────────┼──────────────────┼──────────────────┼───────────┘
          │                  │                  │                  │
          └──────────────────┼──────────────────┼──────────────────┘
                             │                  │
                    ┌────────▼──────────────────▼──────────┐
                    │          REST API Layer              │
                    │  ┌────────────────────────────────┐  │
                    │  │        Spring Boot             │  │
                    │  │     Web Controllers            │  │
                    │  └────────────────────────────────┘  │
                    └────────┬───────────────────────────┘
                             │
    ┌────────────────────────▼────────────────────────────────────────────────┐
    │                    Business Logic Layer                                 │
    │  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────────────┐ │
    │  │   Data          │  │   Statistics    │  │    Scheduler            │ │
    │  │   Processing    │  │   Service       │  │    Service              │ │
    │  │   Service       │  │                 │  │                         │ │
    │  └─────────────────┘  └─────────────────┘  └─────────────────────────┘ │
    └────────┬─────────────────────┬─────────────────────┬────────────────────┘
             │                     │                     │
    ┌────────▼─────────────────────▼─────────────────────▼────────────────────┐
    │                    Data Access Layer                                    │
    │  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────────────┐ │
    │  │   JPA           │  │   Native SQL    │  │   Materialized          │ │
    │  │   Repositories  │  │   Queries       │  │   View Queries          │ │
    │  └─────────────────┘  └─────────────────┘  └─────────────────────────┘ │
    └────────┬─────────────────────────────────────────────────────────────────┘
             │
    ┌────────▼────────────────────────────────────────────────────────────────┐
    │                        Database Layer                                   │
    │  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────────────┐ │
    │  │   Core Tables   │  │   Materialized  │  │   Indexes &             │ │
    │  │   (Raw Data)    │  │   Views         │  │   Constraints           │ │
    │  └─────────────────┘  └─────────────────┘  └─────────────────────────┘ │
    └─────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│                          File System Layer                                 │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────────────────┐ │
│  │   IRNSS Data    │  │   Processed     │  │   Configuration             │ │
│  │   Files         │  │   File Tracker  │  │   Files                     │ │
│  └─────────────────┘  └─────────────────┘  └─────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────────────┘
```

## 🔧 Component Details

### 1. REST API Layer

#### Controllers
- **DataViewController**: Handles data query and visualization endpoints
- **StatisticsController**: Manages analytics and reporting functionality
- **SchedulerController**: Controls materialized view operations and system health

#### Cross-Cutting Concerns
- **CORS Configuration**: Enables cross-origin requests for frontend integration
- **Error Handling**: Global exception handling with structured error responses
- **Request Validation**: Input validation and sanitization
- **Response Formatting**: Consistent JSON response structure

### 2. Business Logic Layer

#### IrnssDataService
```java
@Service
public class IrnssDataService {
    // File monitoring and processing
    // MJD calculation and validation
    // Data extraction and transformation
    // Error handling and recovery
}
```

**Responsibilities**:
- Real-time file system monitoring
- Satellite data file parsing
- MJD (Modified Julian Date) calculations
- Data validation and quality checks
- Incremental processing and resume capability

#### RealFileUploadStatisticsService
```java
@Service
public class RealFileUploadStatisticsService {
    // Statistics aggregation
    // Performance metrics calculation
    // Data quality analysis
    // Reporting functionality
}
```

**Responsibilities**:
- Generate comprehensive file upload statistics
- Calculate processing performance metrics
- Analyze data quality and completeness
- Create business intelligence reports

#### MaterializedViewService
```java
@Service
public class MaterializedViewService {
    // View refresh management
    // Performance optimization
    // Health monitoring
}
```

**Responsibilities**:
- Manage materialized view refresh cycles
- Monitor view refresh performance
- Handle refresh failures and recovery
- Optimize query performance

### 3. Data Access Layer

#### Repository Pattern
```java
@Repository
public interface IrnssDataRepository extends JpaRepository<IrnssData, Long> {
    // Custom queries for data retrieval
    // Performance-optimized native SQL
    // Batch operations
}
```

**Key Repositories**:
- `IrnssDataRepository`: Raw satellite data operations
- `SatCommonViewDifferenceRepository`: Satellite comparison queries
- `SatPivotedViewRepository`: Pivoted data access
- `ProcessedFileRepository`: File processing tracking
- `FileAvailabilityRepository`: File availability queries

#### Query Optimization Strategies
- **Indexed Queries**: All frequently accessed columns are indexed
- **Materialized Views**: Pre-computed aggregations for fast access
- **Batch Operations**: Bulk inserts and updates for large datasets
- **Connection Pooling**: Optimized database connection management

### 4. Database Layer

#### Core Tables
```sql
-- Raw satellite navigation data
CREATE TABLE irnss_data (
    id BIGSERIAL PRIMARY KEY,
    source VARCHAR(10) NOT NULL,
    mjd INTEGER NOT NULL,
    session_number INTEGER,
    -- ... other columns
    INDEX idx_source_mjd (source, mjd),
    INDEX idx_mjd_session (mjd, session_number)
);

-- File processing tracking
CREATE TABLE processed_file (
    id VARCHAR(255) PRIMARY KEY,
    file_path VARCHAR(500) NOT NULL,
    last_processed_line BIGINT DEFAULT 0,
    processing_status VARCHAR(20),
    last_modified TIMESTAMP
);

-- File availability tracking
CREATE TABLE file_availability (
    id BIGSERIAL PRIMARY KEY,
    source VARCHAR(10) NOT NULL,
    mjd INTEGER NOT NULL,
    session_number INTEGER,
    file_name VARCHAR(255),
    is_available BOOLEAN DEFAULT false,
    last_checked TIMESTAMP
);
```

#### Materialized Views
```sql
-- Optimized satellite difference calculations
CREATE MATERIALIZED VIEW sat_common_view_difference_materialized AS
SELECT 
    source, mjd, session_number,
    sat_a_time, sat_b_time,
    (sat_a_time - sat_b_time) as time_difference,
    -- ... complex calculations
FROM irnss_data 
WHERE -- filtering conditions
GROUP BY source, mjd, session_number;

-- Refresh every 5 minutes
CREATE INDEX ON sat_common_view_difference_materialized (source, mjd);
```

## 🔄 Data Flow Architecture

### File Processing Flow
```
File System → File Monitor → Parser → Validator → Database → Statistics
     ↓             ↓           ↓         ↓          ↓          ↓
   IRNSS       Scheduler   Extract   Validate   Raw Data   Analytics
   Files        Service     Data      Quality     Store      Engine
                            ↓                                  ↓
                        MJD Calc                        Materialized
                        Source ID                          Views
                        Session #
```

### API Request Flow
```
Client Request → Controller → Service → Repository → Database
      ↓             ↓          ↓          ↓           ↓
   REST API     Validation  Business   Data       Query
   Endpoint     & Auth      Logic      Access     Execution
      ↓                                            ↓
   Response ← JSON Format ← Transform ← Results ← Raw Data
```

### Statistics Generation Flow
```
Raw Data → Materialized Views → Aggregation Service → Statistics API
    ↓             ↓                    ↓                    ↓
 Real-time    Pre-computed          Business            Formatted
 Updates      Calculations          Intelligence         Response
```

## 🛡️ Security Architecture

### Authentication & Authorization
- **API Security**: Currently open for internal use
- **Database Security**: Connection pooling with credentials
- **File System Security**: Read-only access to data directories

### Data Protection
- **Input Validation**: All user inputs are validated and sanitized
- **SQL Injection Prevention**: Parameterized queries and JPA protection
- **Error Information**: Sanitized error messages to prevent information leakage

## ⚡ Performance Architecture

### Caching Strategy
- **Materialized Views**: Database-level caching for complex queries
- **Application Caching**: Spring Cache abstraction for frequent operations
- **Connection Pooling**: HikariCP for optimized database connections

### Scalability Design
- **Horizontal Scaling**: Stateless service design enables load balancing
- **Database Partitioning**: Tables can be partitioned by source or date
- **Async Processing**: Background tasks for file processing
- **Resource Management**: Proper cleanup and memory management

### Monitoring Points
- **Application Metrics**: Processing rates, error rates, response times
- **Database Metrics**: Query performance, connection usage, view refresh times
- **System Metrics**: CPU, memory, disk usage, network I/O

## 🔧 Configuration Architecture

### Environment Management
```
application.properties (Base)
├── application-dev.properties (Development)
├── application-qa.properties (Quality Assurance)
└── application-prod.properties (Production)
```

### Configuration Hierarchy
1. **Default Configuration**: Base application.properties
2. **Environment-Specific**: Profile-based overrides
3. **External Configuration**: Environment variables and system properties
4. **Runtime Configuration**: JVM arguments and Spring Boot features

## 🚀 Deployment Architecture

### Container Strategy
```dockerfile
FROM openjdk:17-jre-slim
COPY target/tracealibility-*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### Environment Deployment
- **Development**: Local development with embedded configuration
- **QA**: Containerized deployment with external database
- **Production**: Multi-instance deployment with load balancer

## 📊 Monitoring Architecture

### Health Checks
- **Application Health**: Spring Boot Actuator endpoints
- **Database Health**: Connection and query response times
- **File System Health**: Directory access and processing status

### Logging Strategy
- **Application Logs**: Structured logging with SLF4J
- **Access Logs**: HTTP request/response logging
- **Performance Logs**: Processing duration and throughput metrics
- **Error Logs**: Detailed error information with stack traces

---

This architecture provides a robust, scalable, and maintainable foundation for satellite data processing and analytics. The modular design enables easy testing, deployment, and future enhancements.

**Next**: Review [Database Design](Database-Design.md) for detailed schema information.
