# 🗄️ Database Design

Comprehensive documentation of the database schema, relationships, and design decisions for the Time Traceability Backend.

## 📊 Database Overview

- **Database Management System**: PostgreSQL 12+
- **Schema Design**: Normalized with performance-optimized materialized views
- **Character Set**: UTF-8
- **Collation**: en_US.UTF-8
- **Time Zone**: UTC for all timestamps

## 🏗️ Schema Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        Core Data Tables                        │
├─────────────────┬─────────────────┬─────────────────────────────┤
│   irnss_data    │ processed_file  │    file_availability        │
│   (Raw Data)    │ (Processing)    │    (Monitoring)             │
└─────────────────┴─────────────────┴─────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                   Computed Data Views                           │
├─────────────────┬─────────────────┬─────────────────────────────┤
│sat_common_view_ │ sat_pivoted_    │ sat_combined_view_          │
│difference       │ view            │ difference                  │
│(Differences)    │ (Pivot Table)   │ (Combined Analysis)         │
└─────────────────┴─────────────────┴─────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                 Materialized Views (Performance)               │
├─────────────────┬─────────────────┬─────────────────────────────┤
│sat_common_view_ │ sat_pivoted_    │ sat_combined_view_          │
│difference_      │ view_           │ difference_                 │
│materialized     │ materialized    │ materialized                │
└─────────────────┴─────────────────┴─────────────────────────────┘
```

## 📋 Core Tables

### 1. irnss_data - Raw Satellite Data

Primary table storing processed IRNSS satellite navigation data.

```sql
CREATE TABLE irnss_data (
    id BIGSERIAL PRIMARY KEY,
    source VARCHAR(10) NOT NULL,
    mjd INTEGER NOT NULL,
    session_number INTEGER,
    mjd_date_time TIMESTAMP,
    sat_a_time DOUBLE PRECISION,
    sat_b_time DOUBLE PRECISION,
    sat_c_time DOUBLE PRECISION,
    sat_d_time DOUBLE PRECISION,
    sat_e_time DOUBLE PRECISION,
    sat_f_time DOUBLE PRECISION,
    sat_g_time DOUBLE PRECISION,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Indexes for performance
CREATE INDEX idx_irnss_data_source_mjd ON irnss_data (source, mjd);
CREATE INDEX idx_irnss_data_mjd_session ON irnss_data (mjd, session_number);
CREATE INDEX idx_irnss_data_datetime ON irnss_data (mjd_date_time);
CREATE INDEX idx_irnss_data_source_datetime ON irnss_data (source, mjd_date_time);
```

**Field Descriptions:**
- `id`: Unique identifier for each data record
- `source`: Location identifier (e.g., IRLMF, IRXYZ) - first 6 characters of filename
- `mjd`: Modified Julian Date (calculated from filename)
- `session_number`: Session identifier within the day
- `mjd_date_time`: Converted timestamp from MJD
- `sat_*_time`: Timing data for satellites A through G
- `created_at`: Record creation timestamp
- `updated_at`: Last modification timestamp

**Business Rules:**
- Source must be exactly 6 characters
- MJD must be valid (> 0)
- Satellite times are in microseconds
- Each source+mjd+session combination should be unique

### 2. processed_file - File Processing Tracking

Tracks file processing progress to enable incremental processing and prevent duplicates.

```sql
CREATE TABLE processed_file (
    id VARCHAR(255) PRIMARY KEY,
    file_path VARCHAR(500) NOT NULL UNIQUE,
    last_processed_line BIGINT DEFAULT 0,
    total_lines BIGINT,
    processing_status VARCHAR(20) DEFAULT 'PENDING',
    error_message TEXT,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    last_modified TIMESTAMP DEFAULT NOW(),
    file_size_bytes BIGINT,
    checksum VARCHAR(64)
);

-- Indexes
CREATE INDEX idx_processed_file_status ON processed_file (processing_status);
CREATE INDEX idx_processed_file_path ON processed_file (file_path);
CREATE INDEX idx_processed_file_modified ON processed_file (last_modified);
```

**Field Descriptions:**
- `id`: Unique file identifier (usually relative path)
- `file_path`: Full file system path
- `last_processed_line`: Line number for incremental processing
- `total_lines`: Total lines in file (for progress tracking)
- `processing_status`: PENDING, PROCESSING, COMPLETED, FAILED
- `error_message`: Detailed error information if processing failed
- `started_at`: Processing start time
- `completed_at`: Processing completion time
- `checksum`: File integrity verification

**Processing Status Values:**
- `PENDING`: File discovered but not yet processed
- `PROCESSING`: Currently being processed
- `COMPLETED`: Successfully processed
- `FAILED`: Processing failed with errors

### 3. file_availability - File Availability Tracking

Monitors file availability across different sources and dates.

```sql
CREATE TABLE file_availability (
    id BIGSERIAL PRIMARY KEY,
    source VARCHAR(10) NOT NULL,
    mjd INTEGER NOT NULL,
    session_number INTEGER,
    file_name VARCHAR(255),
    file_path VARCHAR(500),
    is_available BOOLEAN DEFAULT false,
    file_size_bytes BIGINT,
    last_checked TIMESTAMP DEFAULT NOW(),
    first_detected TIMESTAMP,
    data_quality_score DECIMAL(5,2),
    processing_duration_ms BIGINT,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Indexes
CREATE UNIQUE INDEX idx_file_avail_source_mjd_session 
    ON file_availability (source, mjd, session_number);
CREATE INDEX idx_file_avail_source_mjd ON file_availability (source, mjd);
CREATE INDEX idx_file_avail_available ON file_availability (is_available);
CREATE INDEX idx_file_avail_last_checked ON file_availability (last_checked);
```

**Field Descriptions:**
- `source`: Location identifier
- `mjd`: Modified Julian Date
- `session_number`: Session within the day
- `file_name`: Original filename
- `is_available`: File availability status
- `data_quality_score`: Data quality assessment (0-100)
- `processing_duration_ms`: Time taken to process file

## 🔍 Computed Views

### 1. sat_common_view_difference - Satellite Time Differences

Calculates time differences between satellites for analysis.

```sql
CREATE VIEW sat_common_view_difference AS
SELECT 
    id,
    source,
    mjd,
    session_number,
    mjd_date_time,
    sat_a_time,
    sat_b_time,
    sat_c_time,
    (sat_a_time - sat_b_time) as sat_a_b_diff,
    (sat_a_time - sat_c_time) as sat_a_c_diff,
    (sat_b_time - sat_c_time) as sat_b_c_diff,
    CASE 
        WHEN ABS(sat_a_time - sat_b_time) > 1000 THEN 'HIGH'
        WHEN ABS(sat_a_time - sat_b_time) > 100 THEN 'MEDIUM'
        ELSE 'LOW'
    END as difference_category
FROM irnss_data 
WHERE sat_a_time IS NOT NULL 
  AND sat_b_time IS NOT NULL 
  AND sat_c_time IS NOT NULL;
```

### 2. sat_pivoted_view - Pivoted Satellite Data

Transforms satellite data into a pivoted format for easier analysis.

```sql
CREATE VIEW sat_pivoted_view AS
SELECT 
    mjd,
    mjd_date_time,
    session_number,
    source,
    'SAT_A' as satellite,
    sat_a_time as satellite_time
FROM irnss_data WHERE sat_a_time IS NOT NULL
UNION ALL
SELECT 
    mjd,
    mjd_date_time,
    session_number,
    source,
    'SAT_B' as satellite,
    sat_b_time as satellite_time
FROM irnss_data WHERE sat_b_time IS NOT NULL
-- ... continue for all satellites
```

## ⚡ Materialized Views

### Purpose and Benefits
Materialized views provide pre-computed results for complex queries, dramatically improving performance for analytics and reporting.

### 1. sat_common_view_difference_materialized

```sql
CREATE MATERIALIZED VIEW sat_common_view_difference_materialized AS
SELECT 
    CONCAT(source, '_', mjd, '_', session_number) as id,
    source,
    mjd,
    session_number,
    mjd_date_time,
    sat_a_time,
    sat_b_time,
    sat_c_time,
    sat_d_time,
    sat_e_time,
    sat_f_time,
    sat_g_time,
    (sat_a_time - sat_b_time) as sat_a_b_diff,
    (sat_a_time - sat_c_time) as sat_a_c_diff,
    (sat_b_time - sat_c_time) as sat_b_c_diff,
    -- Statistical calculations
    STDDEV(sat_a_time) OVER (PARTITION BY source, mjd) as daily_stddev,
    AVG(sat_a_time) OVER (PARTITION BY source, mjd) as daily_avg,
    -- Quality indicators
    CASE 
        WHEN sat_a_time IS NULL OR sat_b_time IS NULL THEN 'POOR'
        WHEN ABS(sat_a_time - sat_b_time) > 1000 THEN 'MODERATE'
        ELSE 'GOOD'
    END as data_quality,
    NOW() as computed_at
FROM irnss_data 
WHERE mjd_date_time >= NOW() - INTERVAL '90 days';

-- Indexes for materialized view
CREATE UNIQUE INDEX idx_sat_diff_mat_id 
    ON sat_common_view_difference_materialized (id);
CREATE INDEX idx_sat_diff_mat_source_mjd 
    ON sat_common_view_difference_materialized (source, mjd);
CREATE INDEX idx_sat_diff_mat_datetime 
    ON sat_common_view_difference_materialized (mjd_date_time);
CREATE INDEX idx_sat_diff_mat_quality 
    ON sat_common_view_difference_materialized (data_quality);
```

### 2. sat_pivoted_view_materialized

```sql
CREATE MATERIALIZED VIEW sat_pivoted_view_materialized AS
SELECT 
    ROW_NUMBER() OVER (ORDER BY mjd, source, session_number, satellite) as id,
    mjd,
    mjd_date_time,
    session_number,
    source,
    satellite,
    satellite_time,
    -- Add derived metrics
    LAG(satellite_time) OVER (
        PARTITION BY satellite, source 
        ORDER BY mjd_date_time
    ) as previous_time,
    satellite_time - LAG(satellite_time) OVER (
        PARTITION BY satellite, source 
        ORDER BY mjd_date_time
    ) as time_delta,
    NOW() as computed_at
FROM (
    SELECT mjd, mjd_date_time, session_number, source, 'SAT_A' as satellite, sat_a_time as satellite_time FROM irnss_data WHERE sat_a_time IS NOT NULL
    UNION ALL
    SELECT mjd, mjd_date_time, session_number, source, 'SAT_B' as satellite, sat_b_time as satellite_time FROM irnss_data WHERE sat_b_time IS NOT NULL
    UNION ALL
    SELECT mjd, mjd_date_time, session_number, source, 'SAT_C' as satellite, sat_c_time as satellite_time FROM irnss_data WHERE sat_c_time IS NOT NULL
    UNION ALL
    SELECT mjd, mjd_date_time, session_number, source, 'SAT_D' as satellite, sat_d_time as satellite_time FROM irnss_data WHERE sat_d_time IS NOT NULL
    UNION ALL
    SELECT mjd, mjd_date_time, session_number, source, 'SAT_E' as satellite, sat_e_time as satellite_time FROM irnss_data WHERE sat_e_time IS NOT NULL
    UNION ALL
    SELECT mjd, mjd_date_time, session_number, source, 'SAT_F' as satellite, sat_f_time as satellite_time FROM irnss_data WHERE sat_f_time IS NOT NULL
    UNION ALL
    SELECT mjd, mjd_date_time, session_number, source, 'SAT_G' as satellite, sat_g_time as satellite_time FROM irnss_data WHERE sat_g_time IS NOT NULL
) pivoted_data
WHERE mjd_date_time >= NOW() - INTERVAL '90 days';

-- Indexes
CREATE UNIQUE INDEX idx_sat_pivot_mat_id 
    ON sat_pivoted_view_materialized (id);
CREATE INDEX idx_sat_pivot_mat_mjd_source 
    ON sat_pivoted_view_materialized (mjd, source);
CREATE INDEX idx_sat_pivot_mat_satellite 
    ON sat_pivoted_view_materialized (satellite);
```

## 🔄 Refresh Strategy

### Automated Refresh Schedule
```sql
-- Refresh every 5 minutes via Spring Scheduler
-- Configured in MaterializedViewScheduler.java

-- Manual refresh commands
REFRESH MATERIALIZED VIEW CONCURRENTLY sat_common_view_difference_materialized;
REFRESH MATERIALIZED VIEW CONCURRENTLY sat_pivoted_view_materialized;
```

### Performance Optimization
- **CONCURRENTLY**: Allows reads during refresh
- **Partial Refresh**: Only processes recent data (90 days)
- **Indexed Views**: All materialized views are properly indexed
- **Incremental Updates**: Uses timestamps to identify new data

## 📈 Performance Considerations

### Query Optimization
1. **Proper Indexing**: All frequently queried columns are indexed
2. **Materialized Views**: Complex calculations are pre-computed
3. **Partitioning Strategy**: Tables can be partitioned by date for larger datasets
4. **Statistics Updates**: Regular ANALYZE commands keep query planner informed

### Index Strategy
```sql
-- Composite indexes for common query patterns
CREATE INDEX idx_irnss_source_mjd_session ON irnss_data (source, mjd, session_number);
CREATE INDEX idx_irnss_datetime_source ON irnss_data (mjd_date_time, source);

-- Partial indexes for performance
CREATE INDEX idx_available_files ON file_availability (mjd, source) WHERE is_available = true;
CREATE INDEX idx_recent_data ON irnss_data (mjd_date_time) WHERE mjd_date_time >= NOW() - INTERVAL '30 days';
```

### Storage Optimization
- **Data Types**: Appropriate data types minimize storage
- **Compression**: Consider table compression for historical data
- **Archival Strategy**: Move old data to archive tables
- **Vacuum Strategy**: Regular maintenance for PostgreSQL

## 🔐 Security and Access Control

### Data Access Patterns
```sql
-- Application user with limited privileges
CREATE USER app_user WITH PASSWORD 'secure_password';
GRANT SELECT, INSERT, UPDATE ON irnss_data TO app_user;
GRANT SELECT, INSERT, UPDATE, DELETE ON processed_file TO app_user;
GRANT SELECT, INSERT, UPDATE ON file_availability TO app_user;
GRANT SELECT ON ALL MATERIALIZED VIEWS TO app_user;

-- Read-only user for reporting
CREATE USER report_user WITH PASSWORD 'report_password';
GRANT SELECT ON ALL TABLES IN SCHEMA public TO report_user;
GRANT SELECT ON ALL MATERIALIZED VIEWS TO report_user;
```

### Data Integrity
```sql
-- Foreign key constraints (if applicable)
-- Check constraints for data validation
ALTER TABLE irnss_data ADD CONSTRAINT chk_mjd_positive CHECK (mjd > 0);
ALTER TABLE irnss_data ADD CONSTRAINT chk_source_length CHECK (LENGTH(source) = 6);

-- Triggers for automatic timestamp updates
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_irnss_data_updated_at 
    BEFORE UPDATE ON irnss_data 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
```

## 📊 Data Lifecycle Management

### Data Retention Policy
- **Active Data**: Keep last 90 days in materialized views
- **Historical Data**: Archive data older than 2 years
- **Processed Files**: Retain processing metadata for 1 year
- **Availability Data**: Keep all availability records

### Backup Strategy
- **Daily Backups**: Full database backup
- **Transaction Log Backup**: Every 15 minutes
- **Point-in-Time Recovery**: Enabled
- **Archive Storage**: Long-term backup retention

### Migration Considerations
- **Schema Versioning**: Use Flyway or Liquibase for migrations
- **Backward Compatibility**: Maintain API compatibility during changes
- **Testing**: Thoroughly test migrations on copy of production data
- **Rollback Plan**: Always have rollback procedures ready

## 🔍 Monitoring and Maintenance

### Database Health Monitoring
```sql
-- Check table sizes
SELECT 
    schemaname,
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) as size,
    pg_total_relation_size(schemaname||'.'||tablename) as size_bytes
FROM pg_tables 
WHERE schemaname = 'public'
ORDER BY size_bytes DESC;

-- Check materialized view freshness
SELECT 
    matviewname,
    ispopulated,
    pg_size_pretty(pg_total_relation_size(matviewname)) as size
FROM pg_matviews;

-- Monitor query performance
SELECT 
    query,
    calls,
    total_time,
    mean_time,
    rows
FROM pg_stat_statements
ORDER BY mean_time DESC
LIMIT 10;
```

### Maintenance Tasks
- **Daily**: Update statistics, check for slow queries
- **Weekly**: Reindex if needed, check database size growth
- **Monthly**: Review materialized view performance, optimize queries
- **Quarterly**: Review archival strategy, update retention policies

---

This database design provides a robust foundation for satellite data processing with optimized performance through materialized views and proper indexing. The schema supports both real-time data ingestion and complex analytics queries.

**Next**: Review [Performance Optimization](Performance-Optimization.md) for query tuning strategies.
