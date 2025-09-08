# ❓ Frequently Asked Questions (FAQ)

Common questions and answers about the Time Traceability Backend system.

## 🚀 General Questions

### Q: What is the Time Traceability Backend?
**A:** The Time Traceability Backend is a Spring Boot application that processes IRNSS (Indian Regional Navigation Satellite System) satellite data files in real-time, providing analytics and APIs for satellite timing analysis and file availability tracking.

### Q: What file formats does the system support?
**A:** The system processes IRNSS data files with naming pattern `GZ[SOURCE][SESSION][MJD]` (e.g., `GZLI2P60.866`). Files contain satellite timing data in space-separated format.

### Q: How often does the system check for new files?
**A:** The system scans directories every 5 minutes by default. This can be configured via the `irnss.scheduler.fixed-rate` property.

### Q: What happens if a file processing fails?
**A:** The system automatically retries failed files up to 3 times (configurable). If all retries fail, the file is marked as "FAILED" and can be manually reprocessed later.

## 🔧 Technical Questions

### Q: How is MJD (Modified Julian Date) calculated?
**A:** MJD is extracted from the filename by concatenating all digits after the first 6 characters. For example, `GZLI2P60.866` extracts digits `60866` as the MJD. The MJD epoch is November 17, 1858.

### Q: What databases are supported?
**A:** Currently, the system supports PostgreSQL 12+. The database schema includes materialized views for performance optimization.

### Q: How does the system handle character encoding issues?
**A:** The system first attempts to read files with UTF-8 encoding. If that fails, it automatically falls back to ISO-8859-1 encoding to handle legacy file formats.

### Q: What is the purpose of materialized views?
**A:** Materialized views pre-compute complex satellite difference calculations and aggregations, dramatically improving API response times for analytics queries. They refresh every 5 minutes.

## 📊 Data Questions

### Q: What data is extracted from each file?
**A:** From each file, the system extracts:
- Source location (first 6 characters of filename)
- MJD (Modified Julian Date)
- Session number
- Satellite timing data (Satellites A through G)
- File processing metadata

### Q: How is data quality ensured?
**A:** The system includes multiple validation layers:
- File format validation
- Numeric data validation
- Range checks for satellite timestamps
- Data completeness scoring
- Error tracking and reporting

### Q: Can I reprocess files if needed?
**A:** Yes, you can reset file processing status in the database:
```sql
UPDATE processed_file 
SET processing_status = 'PENDING', last_processed_line = 0 
WHERE file_path = '/path/to/your/file';
```

### Q: How long is data retained?
**A:** 
- **Active data**: Last 90 days in materialized views for fast access
- **Historical data**: All data is retained in base tables
- **Processing metadata**: Kept for 1 year
- **File availability data**: Retained indefinitely

## 🔌 API Questions

### Q: What APIs are available?
**A:** The system provides three main API categories:
- **Data APIs** (`/api/data/*`): Query satellite data and differences
- **Statistics APIs** (`/api/statistics/*`): Analytics and reporting
- **System APIs** (`/api/scheduler/*`): Health and management

### Q: Is authentication required for APIs?
**A:** Currently, the APIs are open for internal use. Future versions may include API key authentication for external access.

### Q: What response format do APIs use?
**A:** All APIs return JSON responses with consistent error handling and pagination for large datasets.

### Q: How do I filter data by date range?
**A:** Most APIs accept `startDate` and `endDate` parameters in ISO format:
```
GET /api/data/bulk-location-data?startDate=2025-01-01&endDate=2025-12-31
```

## ⚡ Performance Questions

### Q: How fast does the system process files?
**A:** Processing speed depends on file size and content:
- **Small files** (< 1MB): 200-500ms
- **Medium files** (1-10MB): 1-5 seconds
- **Large files** (> 10MB): 5-30 seconds

### Q: What are the system requirements?
**A:** **Minimum requirements:**
- Java 17+
- PostgreSQL 12+
- 2GB RAM
- 10GB disk space

**Recommended for production:**
- 4GB+ RAM
- SSD storage
- Multi-core CPU

### Q: How can I improve performance?
**A:** Several optimization options:
- Increase JVM heap size: `-Xmx4g`
- Enable parallel processing: `irnss.processing.parallel-directories=true`
- Optimize database: Regular `VACUUM ANALYZE`
- Monitor materialized view refresh times

## 🛠️ Configuration Questions

### Q: How do I change the data directory path?
**A:** Update the `irnss.parent-folder` property in `application.properties`:
```properties
irnss.parent-folder=/new/path/to/satellite/data
```

### Q: Can I disable automatic file processing?
**A:** Yes, set the scheduler to disabled:
```properties
irnss.scheduler.enabled=false
```

### Q: How do I change the database connection?
**A:** Update the datasource properties:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/time_traceability
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### Q: Can I run multiple instances?
**A:** Yes, the application is stateless and can be horizontally scaled. Ensure each instance:
- Connects to the same database
- Has access to the same file system
- Uses a load balancer for API access

## 🚨 Troubleshooting Questions

### Q: The application won't start. What should I check?
**A:** Common startup issues:
1. **Database connectivity**: Verify PostgreSQL is running and accessible
2. **Java version**: Ensure Java 17+ is installed
3. **Port conflicts**: Check if port 6003 is available
4. **Configuration**: Verify application.properties settings

### Q: Files are detected but not processed. Why?
**A:** Possible causes:
1. **File permissions**: Ensure read access to data directory
2. **File format**: Verify files match expected naming pattern
3. **Character encoding**: Check for encoding issues in files
4. **Processing stuck**: Check for files stuck in "PROCESSING" status

### Q: API responses are slow. How to diagnose?
**A:** Performance troubleshooting steps:
1. Check materialized view freshness: `/api/scheduler/status`
2. Monitor database query performance
3. Verify system resources (CPU, memory, disk)
4. Check for slow queries in PostgreSQL logs

### Q: How do I check system health?
**A:** Use the health endpoint:
```bash
curl http://localhost:6003/time-traceability-service/api/scheduler/health
```

## 📈 Monitoring Questions

### Q: What metrics should I monitor?
**A:** Key metrics to track:
- **File processing rate**: Files processed per hour
- **API response times**: Average response time < 2 seconds
- **Database performance**: Query execution times
- **Error rates**: Processing failures < 1%
- **System resources**: CPU, memory, disk usage

### Q: How do I get processing statistics?
**A:** Use the statistics API:
```bash
curl "http://localhost:6003/time-traceability-service/api/statistics/file-upload-stats"
```

### Q: Can I set up alerts?
**A:** Yes, monitor these endpoints for alerts:
- Health check failures: `/api/scheduler/health`
- High error rates in statistics API
- Database connection issues
- File processing delays

## 🔄 Data Recovery Questions

### Q: What if I accidentally delete data?
**A:** Recovery options:
1. **Database backup**: Restore from daily backups
2. **File reprocessing**: Reset processed_file status to reprocess
3. **Materialized view refresh**: Refresh views if data exists in base tables

### Q: How do I handle corrupted files?
**A:** For corrupted files:
1. **Skip processing**: Mark file as "FAILED" manually
2. **Fix encoding**: Try different character encodings
3. **Manual processing**: Extract data manually if critical

### Q: Can I migrate to a different database?
**A:** Yes, the system uses JPA, making database migration possible:
1. Export data from current database
2. Set up new database with same schema
3. Import data and update configuration
4. Test thoroughly before switching

## 🚀 Development Questions

### Q: How do I contribute to the project?
**A:** Follow the contribution process:
1. Fork the repository
2. Create a feature branch
3. Follow coding standards
4. Add tests for new features
5. Submit a pull request

### Q: What testing is required?
**A:** Testing requirements:
- **Unit tests**: For business logic
- **Integration tests**: For database operations
- **API tests**: For endpoint functionality
- **Performance tests**: For large datasets

### Q: How do I debug issues locally?
**A:** Development debugging:
1. **Enable debug logging**: Set log level to DEBUG
2. **Use IDE debugger**: Step through code execution
3. **Check database state**: Query tables directly
4. **Monitor file processing**: Watch logs during processing

---

**Still have questions?** 
- Check the [Troubleshooting Guide](Troubleshooting.md)
- Review the [API Reference](API-Reference.md)
- Contact the development team
- Create an issue in the repository

**This FAQ is updated regularly based on user questions and system changes.**
