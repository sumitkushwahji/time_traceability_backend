# 🔧 Troubleshooting Guide

Comprehensive troubleshooting guide for common issues, error resolution, and system diagnostics.

## 🚨 Common Issues & Solutions

### 1. Application Startup Issues

#### Error: "Failed to configure a DataSource"
```
Description:
Failed to configure a DataSource: 'url' attribute is not specified and no embedded datasource could be configured.
```

**Solution:**
```bash
# Check application.properties
spring.datasource.url=jdbc:postgresql://localhost:5432/time_traceability
spring.datasource.username=postgres
spring.datasource.password=postgres

# Verify PostgreSQL is running
pg_isready -h localhost -p 5432

# Test database connection
psql -h localhost -U postgres -d time_traceability
```

#### Error: "Port 6003 is already in use"
```
Web server failed to start. Port 6003 was already in use.
```

**Solution:**
```bash
# Find process using port 6003
netstat -tulpn | grep 6003
# or on Windows
netstat -ano | findstr :6003

# Kill the process
kill -9 <PID>
# or on Windows
taskkill /PID <PID> /F

# Or change port in application.properties
server.port=6004
```

### 2. Database Connection Issues

#### Error: "Connection to database failed"
```
org.postgresql.util.PSQLException: Connection to localhost:5432 refused
```

**Diagnosis Steps:**
```bash
# 1. Check if PostgreSQL is running
sudo systemctl status postgresql
# or on Windows
sc query postgresql-x64-12

# 2. Check PostgreSQL configuration
sudo -u postgres psql -c "SHOW listen_addresses;"

# 3. Check pg_hba.conf for authentication
sudo nano /etc/postgresql/12/main/pg_hba.conf

# 4. Test connection manually
psql -h localhost -U postgres -d time_traceability
```

**Solutions:**
- Start PostgreSQL service: `sudo systemctl start postgresql`
- Update `listen_addresses = '*'` in postgresql.conf
- Add appropriate entries to pg_hba.conf
- Verify firewall settings

#### Error: "password authentication failed"
```
org.postgresql.util.PSQLException: FATAL: password authentication failed for user "postgres"
```

**Solution:**
```bash
# Reset PostgreSQL password
sudo -u postgres psql
ALTER USER postgres PASSWORD 'new_password';

# Update application.properties
spring.datasource.password=new_password
```

### 3. File Processing Issues

#### Error: "Directory not found or not accessible"
```
java.nio.file.NoSuchFileException: /path/to/satellite/data
```

**Diagnosis:**
```bash
# Check if directory exists
ls -la /path/to/satellite/data

# Check permissions
ls -ld /path/to/satellite/data

# Check application configuration
grep irnss.parent-folder src/main/resources/application.properties
```

**Solutions:**
```bash
# Create directory if missing
mkdir -p /path/to/satellite/data

# Fix permissions
chmod 755 /path/to/satellite/data
chown app_user:app_group /path/to/satellite/data

# Update configuration
irnss.parent-folder=/correct/path/to/data
```

#### Error: "Character encoding issues"
```
java.nio.charset.MalformedInputException: Input length = 1
```

**Solution:**
The application automatically handles this by falling back to ISO-8859-1 encoding. If issues persist:

```java
// Check IrnssDataService.java for encoding fallback logic
try {
    lines = Files.readAllLines(file, StandardCharsets.UTF_8);
} catch (MalformedInputException e) {
    lines = Files.readAllLines(file, StandardCharsets.ISO_8859_1);
}
```

#### Error: "File processing stuck"
```
Files are detected but not processed, or processing takes too long
```

**Diagnosis:**
```bash
# Check application logs
tail -f logs/application.log

# Check processed_file table
SELECT * FROM processed_file WHERE processing_status = 'PROCESSING';

# Check system resources
top
df -h
```

**Solutions:**
```sql
-- Reset stuck processing
UPDATE processed_file 
SET processing_status = 'PENDING', 
    last_processed_line = 0 
WHERE processing_status = 'PROCESSING' 
  AND started_at < NOW() - INTERVAL '1 hour';
```

### 4. Performance Issues

#### Slow API Response Times
```
API endpoints responding slowly (>5 seconds)
```

**Diagnosis:**
```sql
-- Check slow queries
SELECT query, calls, total_time, mean_time 
FROM pg_stat_statements 
ORDER BY mean_time DESC 
LIMIT 10;

-- Check materialized view freshness
SELECT matviewname, pg_size_pretty(pg_total_relation_size(matviewname)) as size
FROM pg_matviews;

-- Check database connections
SELECT count(*) as active_connections 
FROM pg_stat_activity 
WHERE state = 'active';
```

**Solutions:**
```bash
# Refresh materialized views manually
curl -X POST "http://localhost:6003/time-traceability-service/api/scheduler/refresh"

# Restart application to reset connection pool
systemctl restart time-traceability-backend

# Optimize database
sudo -u postgres psql -d time_traceability -c "VACUUM ANALYZE;"
```

#### High Memory Usage
```
Application consuming excessive memory (>2GB)
```

**Diagnosis:**
```bash
# Check Java heap usage
jstat -gc <PID>

# Check application metrics
curl "http://localhost:6003/time-traceability-service/actuator/metrics/jvm.memory.used"

# Check for memory leaks
jmap -histo <PID> | head -20
```

**Solutions:**
```bash
# Increase heap size
export JAVA_OPTS="-Xmx4g -Xms2g"

# Enable garbage collection logging
export JAVA_OPTS="$JAVA_OPTS -XX:+PrintGC -XX:+PrintGCDetails"

# Restart with new settings
systemctl restart time-traceability-backend
```

### 5. Materialized View Issues

#### Error: "Materialized view refresh failed"
```
ERROR: could not refresh materialized view concurrently
```

**Diagnosis:**
```sql
-- Check view status
SELECT matviewname, ispopulated FROM pg_matviews;

-- Check for locks
SELECT * FROM pg_locks WHERE locktype = 'relation';

-- Check view dependencies
SELECT * FROM pg_depend WHERE objid IN (
    SELECT oid FROM pg_class WHERE relname LIKE '%materialized%'
);
```

**Solutions:**
```sql
-- Force refresh without CONCURRENTLY
REFRESH MATERIALIZED VIEW sat_common_view_difference_materialized;

-- Recreate view if corrupted
DROP MATERIALIZED VIEW sat_common_view_difference_materialized;
-- Then recreate from schema

-- Check and fix indexes
REINDEX INDEX CONCURRENTLY idx_sat_diff_mat_source_mjd;
```

### 6. API Error Responses

#### Error: 500 Internal Server Error
```json
{
  "timestamp": "2025-09-08T12:00:00.000+00:00",
  "status": 500,
  "error": "Internal Server Error",
  "path": "/api/data/bulk-location-data"
}
```

**Diagnosis:**
```bash
# Check application logs
tail -f logs/application.log | grep ERROR

# Check database connectivity
curl "http://localhost:6003/time-traceability-service/api/scheduler/health"

# Check specific endpoint
curl -v "http://localhost:6003/time-traceability-service/api/data/available-mjds"
```

#### Error: 503 Service Unavailable
```json
{
  "status": 503,
  "error": "Service Unavailable",
  "message": "Database is temporarily unavailable"
}
```

**Solutions:**
```bash
# Check database status
pg_isready -h localhost -p 5432

# Restart database if needed
sudo systemctl restart postgresql

# Check connection pool
# Look for connection pool exhaustion in logs
```

## 🔍 Diagnostic Commands

### System Health Check
```bash
#!/bin/bash
echo "=== Time Traceability Backend Health Check ==="

# 1. Application Status
echo "1. Application Status:"
curl -s "http://localhost:6003/time-traceability-service/api/scheduler/health" | jq .

# 2. Database Connectivity
echo "2. Database Status:"
pg_isready -h localhost -p 5432 && echo "Database is ready" || echo "Database is not accessible"

# 3. File System Check
echo "3. File System Status:"
ls -la /path/to/satellite/data 2>/dev/null && echo "Data directory accessible" || echo "Data directory not found"

# 4. Process Status
echo "4. Process Status:"
ps aux | grep java | grep tracealibility

# 5. Port Status
echo "5. Port Status:"
netstat -tulpn | grep :6003

# 6. Log Summary
echo "6. Recent Errors:"
tail -50 logs/application.log | grep ERROR | tail -5
```

### Database Diagnostic Queries
```sql
-- Database size and table information
SELECT 
    schemaname,
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) as size,
    pg_total_relation_size(schemaname||'.'||tablename) as size_bytes,
    (SELECT count(*) FROM information_schema.columns 
     WHERE table_name = tablename AND table_schema = schemaname) as column_count
FROM pg_tables 
WHERE schemaname = 'public'
ORDER BY size_bytes DESC;

-- Active connections and queries
SELECT 
    pid,
    usename,
    application_name,
    client_addr,
    state,
    query_start,
    LEFT(query, 50) as query_preview
FROM pg_stat_activity 
WHERE state != 'idle'
ORDER BY query_start;

-- Recent processing status
SELECT 
    processing_status,
    count(*) as file_count,
    avg(EXTRACT(EPOCH FROM (completed_at - started_at))) as avg_processing_time_seconds
FROM processed_file 
WHERE started_at >= NOW() - INTERVAL '24 hours'
GROUP BY processing_status;

-- Data quality check
SELECT 
    source,
    count(*) as total_records,
    count(*) FILTER (WHERE sat_a_time IS NOT NULL) as sat_a_records,
    count(*) FILTER (WHERE sat_b_time IS NOT NULL) as sat_b_records,
    round(100.0 * count(*) FILTER (WHERE sat_a_time IS NOT NULL) / count(*), 2) as sat_a_completeness
FROM irnss_data 
WHERE mjd_date_time >= NOW() - INTERVAL '7 days'
GROUP BY source
ORDER BY source;
```

### Performance Monitoring
```bash
# Monitor API response times
curl -w "Time: %{time_total}s\n" -o /dev/null -s \
  "http://localhost:6003/time-traceability-service/api/data/available-mjds"

# Monitor database query performance
sudo -u postgres psql -d time_traceability -c "
SELECT 
    query,
    calls,
    total_time/1000 as total_time_seconds,
    mean_time as mean_time_ms,
    (100 * total_time / sum(total_time) OVER()) as percentage
FROM pg_stat_statements 
WHERE calls > 10
ORDER BY total_time DESC 
LIMIT 10;"

# Monitor file processing rate
watch -n 5 "echo 'Processing Status:'; psql -h localhost -U postgres -d time_traceability -c \"SELECT processing_status, count(*) FROM processed_file GROUP BY processing_status;\""
```

## 🛠️ Recovery Procedures

### Database Recovery
```bash
# 1. Stop application
systemctl stop time-traceability-backend

# 2. Backup current state
pg_dump -h localhost -U postgres time_traceability > backup_$(date +%Y%m%d_%H%M%S).sql

# 3. Reset problematic data
sudo -u postgres psql -d time_traceability -c "
UPDATE processed_file 
SET processing_status = 'PENDING', last_processed_line = 0 
WHERE processing_status = 'PROCESSING';"

# 4. Refresh materialized views
sudo -u postgres psql -d time_traceability -c "
REFRESH MATERIALIZED VIEW sat_common_view_difference_materialized;
REFRESH MATERIALIZED VIEW sat_pivoted_view_materialized;"

# 5. Restart application
systemctl start time-traceability-backend
```

### Application Recovery
```bash
# 1. Clear application cache (if using Redis)
redis-cli FLUSHALL

# 2. Reset file processing
curl -X POST "http://localhost:6003/time-traceability-service/api/scheduler/refresh"

# 3. Verify system health
curl "http://localhost:6003/time-traceability-service/api/scheduler/health"
```

## 📞 Escalation Procedures

### Level 1: Basic Issues
- Database connection problems
- File permission issues
- Configuration errors
- API timeout errors

**Actions:**
1. Check system health endpoints
2. Restart application service
3. Verify configuration files
4. Check system resources

### Level 2: Performance Issues
- Slow query performance
- High memory usage
- Database lock issues
- Materialized view problems

**Actions:**
1. Analyze database performance
2. Review and optimize queries
3. Check materialized view refresh
4. Monitor system resources

### Level 3: Data Integrity Issues
- Data corruption
- Processing failures
- Inconsistent results
- System failures

**Actions:**
1. Stop data processing
2. Backup current state
3. Analyze data integrity
4. Implement recovery procedures
5. Contact development team

## 📋 Maintenance Checklist

### Daily Checks
- [ ] Verify application is running
- [ ] Check API health endpoints
- [ ] Review error logs
- [ ] Monitor file processing rate
- [ ] Check database connectivity

### Weekly Checks
- [ ] Review system performance metrics
- [ ] Check database query performance
- [ ] Verify materialized view refresh
- [ ] Review disk usage
- [ ] Update system statistics

### Monthly Checks
- [ ] Review and optimize slow queries
- [ ] Check database size growth
- [ ] Review file processing efficiency
- [ ] Update documentation
- [ ] Plan for system upgrades

---

This troubleshooting guide covers the most common issues encountered with the Time Traceability Backend. For issues not covered here, check the [System Architecture](System-Architecture.md) for system details or contact the development team.

**Emergency Contact**: Development Team  
**On-Call Support**: Available 24/7 for production issues
