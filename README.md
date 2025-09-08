# 🛰️ Time Traceability Backend

A robust Spring Boot application for satellite data ingestion, processing, and analytics. This backend service handles real-time monitoring of IRNSS (Indian Regional Navigation Satellite System) data files and provides comprehensive statistics and data visualization APIs.

## 📋 Table of Contents

- [Features](#-features)
- [Architecture](#-architecture)
- [Technologies](#-technologies)
- [Project Structure](#-project-structure)
- [API Endpoints](#-api-endpoints)
- [Database Schema](#-database-schema)
- [Installation](#-installation)
- [Configuration](#-configuration)
- [Usage](#-usage)
- [Development](#-development)
- [Performance Optimizations](#-performance-optimizations)
- [Monitoring](#-monitoring)

## ✨ Features

### 🔄 Real-time Data Processing
- **Automated File Monitoring**: Scans directories every 5 minutes for new satellite data files
- **Parallel Processing**: Multi-threaded directory scanning for optimal performance
- **Incremental Processing**: Resumes file processing from the last processed line
- **Fault-Tolerant**: Continues processing other files when errors occur

### 📊 Advanced Analytics
- **File Upload Statistics**: Comprehensive metrics on data ingestion
- **Location-based Analysis**: Statistics per satellite monitoring station
- **Time Series Data**: Daily trends and historical analysis
- **Quality Metrics**: Data completeness and availability reports

### 🚀 High Performance
- **Materialized Views**: Pre-computed data for fast query responses
- **Optimized Queries**: Indexed database operations
- **Bulk Operations**: Efficient batch processing
- **Resource Management**: Proper cleanup and memory optimization

### 🛡️ Robust Error Handling
- **Character Encoding Fallback**: UTF-8 → ISO-8859-1 automatic fallback
- **File Validation**: Skip empty or corrupted files
- **Graceful Degradation**: Service continues despite individual file errors
- **Comprehensive Logging**: Detailed error tracking and debugging info

## 🏗️ Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   File System   │───▶│  Data Ingestion │───▶│    Database     │
│                 │    │   (IrnssData    │    │                 │
│ • IRNSS Files   │    │    Service)     │    │ • Raw Data      │
│ • Location Dirs │    │                 │    │ • Processed     │
│ • Real-time     │    │ • Monitoring    │    │ • Statistics    │
└─────────────────┘    │ • Parsing       │    └─────────────────┘
                       │ • Validation    │             │
                       └─────────────────┘             │
                                                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│  REST APIs      │◀───│   Controllers   │◀───│ Materialized    │
│                 │    │                 │    │    Views        │
│ • Statistics    │    │ • Data View     │    │                 │
│ • Data Query    │    │ • Statistics    │    │ • Aggregated    │
│ • Scheduler     │    │ • Scheduler     │    │ • Optimized     │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## 🛠️ Technologies

- **Java 17+** - Core programming language
- **Spring Boot 3.x** - Application framework
- **Spring Data JPA** - Database abstraction layer
- **PostgreSQL** - Primary database
- **Maven** - Dependency management
- **Spring Scheduler** - Automated task execution
- **Jackson** - JSON processing
- **SLF4J** - Logging framework

## 📁 Project Structure

```
src/main/java/com/time/tracealibility/
├── 🎮 controllers/
│   ├── DataViewController.java      # Data query and visualization endpoints
│   ├── SchedulerController.java     # Materialized view management
│   └── StatisticsController.java    # Analytics and reporting endpoints
├── 🧩 services/
│   ├── IrnssDataService.java       # File processing and data ingestion
│   ├── RealFileUploadStatisticsService.java  # Analytics generation
│   └── MaterializedViewService.java # Database view refresh
├── 🗄️ repository/
│   ├── IrnssDataRepository.java
│   ├── SatCommonViewDifferenceRepository.java
│   ├── SatPivotedViewRepository.java
│   ├── ProcessedFileRepository.java
│   └── FileAvailabilityRepository.java
├── 🏛️ entity/
│   ├── IrnssData.java              # Raw satellite data
│   ├── SatCommonViewDifference.java # Satellite comparisons
│   ├── SatPivotedView.java         # Pivoted data view
│   ├── ProcessedFile.java          # File processing tracking
│   └── FileAvailability.java      # File availability tracking
├── 📊 dto/
│   ├── FileUploadStatsDTO.java     # Statistics response
│   └── SourceSessionStatusDTO.java # Session completeness data
├── ⚙️ config/
│   ├── CorsConfig.java             # Cross-origin configuration
│   └── MaterializedViewStartupCheck.java # System health checks
└── 📅 scheduler/
    └── MaterializedViewScheduler.java    # Automated view refresh
```

## 🌐 API Endpoints

### Data Query APIs
```http
GET /api/data/bulk-location-data
    ?source2=IRLMF,IRXYZ&startDate=2025-01-01&endDate=2025-12-31
    # Bulk data retrieval for specific locations

GET /api/data/optimized-sat-differences
    ?page=0&size=20&sortBy=mjdDateTime&sortDirection=desc
    # Paginated satellite difference data

GET /api/data/session-completeness
    ?source=IRLMF&mjd=60000
    # Session completeness analysis

GET /api/data/available-mjds
    # List of available Modified Julian Days

GET /api/data/pivoted-sat-data
    ?page=0&size=15&sortBy=mjd_date_time&sortDirection=desc
    # Pivoted satellite data view
```

### Statistics APIs
```http
GET /api/statistics/file-upload-stats
    ?startDate=2025-01-01T00:00:00&endDate=2025-12-31T23:59:59
    # Comprehensive file upload and processing statistics

GET /api/statistics/file-upload-stats/location/{locationName}
    # Location-specific statistics

GET /api/statistics/file-upload-stats/mjd/{mjd}
    # MJD-specific statistics
```

### System Management APIs
```http
GET /api/scheduler/status
    # Materialized view refresh status

POST /api/scheduler/refresh
    # Manual materialized view refresh

GET /api/scheduler/health
    # System health check
```

## 🗃️ Database Schema

### Core Tables
- **`irnss_data`** - Raw satellite navigation data
- **`processed_file`** - File processing progress tracking
- **`file_availability`** - File availability and status
- **`sat_common_view_difference`** - Satellite time differences
- **`sat_pivoted_view`** - Pivoted satellite data for analysis

### Materialized Views
- **`sat_common_view_difference_materialized`** - Optimized satellite analysis data
- **`sat_pivoted_view_materialized`** - Pre-computed pivoted data
- **`sat_combined_view_difference_materialized`** - Combined satellite differences

## 🚀 Installation

### Prerequisites
- Java 17 or higher
- PostgreSQL 12+
- Maven 3.6+

### Setup Steps

1. **Clone the repository**
   ```bash
   git clone https://github.com/sumitkushwahji/time_traceability_backend.git
   cd time_traceability_backend
   ```

2. **Database Setup**
   ```sql
   CREATE DATABASE time_traceability;
   CREATE USER traceability_user WITH PASSWORD 'your_password';
   GRANT ALL PRIVILEGES ON DATABASE time_traceability TO traceability_user;
   ```

3. **Configure Application**
   ```bash
   # Edit src/main/resources/application.properties
   # Or use environment-specific profiles
   ```

4. **Build and Run**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

## ⚙️ Configuration

### Application Properties
```properties
# Server Configuration
server.port=6003
server.servlet.context-path=/time-traceability-service

# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/time_traceability
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.jpa.hibernate.ddl-auto=update

# File Processing Configuration
irnss.parent-folder=/path/to/satellite/data

# Scheduler Configuration
app.scheduler.materialized-view.enabled=true
app.scheduler.materialized-view.refresh-interval=300000
app.scheduler.materialized-view.names=sat_common_view_difference_materialized,sat_pivoted_view_materialized,sat_combined_view_difference_materialized
```

### Environment Profiles
- **Development**: `application-dev.properties`
- **QA**: `application-qa.properties`
- **Production**: `application-prod.properties`

### Directory Structure
```
/path/to/satellite/data/
├── IRLMF/          # Location 1 files
├── IRXYZ/          # Location 2 files
└── IRABC/          # Location 3 files
```

## 📖 Usage

### Starting the Application
```bash
# Development mode
mvn spring-boot:run -Dspring.profiles.active=dev

# Production mode with JAR
java -jar target/tracealibility-0.0.1-SNAPSHOT.jar

# With custom profile
java -jar target/tracealibility-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

### Docker Deployment
```bash
# Build Docker image
docker build -t time-traceability-backend .

# Run container
docker run -d -p 6003:6003 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host:5432/db \
  -e SPRING_DATASOURCE_USERNAME=user \
  -e SPRING_DATASOURCE_PASSWORD=password \
  time-traceability-backend
```

### Monitoring File Processing
```bash
# Check processing logs
tail -f logs/application.log

# View statistics
curl http://localhost:6003/time-traceability-service/api/statistics/file-upload-stats

# Check system health
curl http://localhost:6003/time-traceability-service/api/scheduler/health
```

## 👨‍💻 Development

### Running Tests
```bash
mvn test
```

### Building for Production
```bash
mvn clean package -Pprod
```

### Local Development Setup
```bash
# Start PostgreSQL locally
docker run -d --name postgres \
  -e POSTGRES_DB=time_traceability \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 postgres:13

# Run application in dev mode
mvn spring-boot:run -Dspring.profiles.active=dev
```

## ⚡ Performance Optimizations

### Database Optimizations
- **Indexed Columns**: Primary sorting and filtering columns
- **Materialized Views**: Pre-computed aggregations refreshed every 5 minutes
- **Connection Pooling**: Optimized database connections
- **Batch Processing**: Bulk insert operations for file data

### Application Optimizations
- **Parallel Processing**: Multi-threaded directory scanning
- **Memory Management**: Proper resource cleanup and Scanner handling
- **Incremental Processing**: Resume from last processed file position
- **Early Filtering**: Skip empty/problematic files before processing
- **Character Encoding Resilience**: UTF-8 → ISO-8859-1 fallback

### Monitoring Optimizations
- **Scheduled Refresh**: Automated materialized view updates
- **Health Checks**: System status monitoring
- **Performance Metrics**: Processing statistics tracking
- **Execution Timing**: Monitor processing duration

## 📊 Monitoring

### Application Metrics
- Files processed per minute
- Database query performance
- Memory and CPU usage
- Error rates and types
- Processing duration tracking

### Business Metrics
- Data completeness by location
- File upload trends over time
- Quality metrics and availability
- Session completeness analysis
- MJD coverage statistics

### Health Endpoints
```http
GET /api/scheduler/health       # System health status
GET /api/scheduler/status       # Materialized view processing status
GET /api/statistics/file-upload-stats  # Operational metrics and statistics
```

### Key Performance Indicators
- **File Processing Rate**: Files processed per hour
- **Data Quality Score**: Percentage of successful data ingestion
- **System Uptime**: Service availability metrics
- **Error Rate**: Processing failures per total attempts

## 🚨 Troubleshooting

### Common Issues

1. **Character Encoding Errors**
   - Solution: Service automatically falls back to ISO-8859-1 encoding

2. **File Processing Stuck**
   - Check: File permissions and disk space
   - Monitor: Processing logs for specific error messages

3. **Database Connection Issues**
   - Verify: Database credentials and connectivity
   - Check: Connection pool configuration

4. **Materialized View Refresh Failures**
   - Monitor: `/api/scheduler/status` endpoint
   - Manual trigger: `/api/scheduler/refresh` endpoint

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Code Style Guidelines
- Follow Spring Boot best practices
- Use meaningful variable and method names
- Add comprehensive error handling
- Include unit tests for new features
- Document complex business logic

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📞 Support

For support and questions:
- **📚 Documentation**: Check the [Project Wiki](wiki/) for comprehensive guides
- **❓ Quick Help**: Review [FAQ](wiki/FAQ.md) for common questions  
- **🔧 Issues**: See [Troubleshooting Guide](wiki/Troubleshooting.md)
- **📋 Bug Reports**: Create an issue in the repository
- **💬 Contact**: Reach out to the development team

## 📚 Project Wiki

Our comprehensive documentation is available in the [project wiki](wiki/):

### 🚀 Quick Access
- **[Quick Start Guide](wiki/Quick-Start-Guide.md)** - Get running in 5 minutes
- **[API Reference](wiki/API-Reference.md)** - Complete API documentation
- **[Troubleshooting](wiki/Troubleshooting.md)** - Common issues and solutions
- **[System Architecture](wiki/System-Architecture.md)** - Technical overview

### 📖 Full Documentation
Visit the **[Wiki Home](wiki/Home.md)** for the complete documentation index covering:
- Installation and configuration guides
- System architecture and database design  
- Development workflows and coding standards
- Performance optimization and monitoring
- Data processing logic and file handling
- FAQ and glossary of technical terms

---

**Built with ❤️ for satellite data processing and time traceability analysis**

*Last updated: September 2025*
