-- Enhanced migration script to fix deadlock issues in production
-- Run this script on your PostgreSQL database

-- Step 1: Remove existing duplicate records (keep the latest one for each source+mjd combination)
DELETE FROM file_availability 
WHERE id NOT IN (
    SELECT DISTINCT ON (source, mjd) id 
    FROM file_availability 
    ORDER BY source, mjd, timestamp DESC
);

-- Step 2: Add unique constraint to prevent future duplicates
ALTER TABLE file_availability 
ADD CONSTRAINT uk_file_availability_source_mjd 
UNIQUE (source, mjd);

-- Step 3: Create optimized indexes for deadlock prevention
-- These indexes help PostgreSQL acquire locks in a consistent order, reducing deadlocks

-- Index for file_availability table
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_file_availability_source_mjd_status 
ON file_availability (source, mjd, status);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_file_availability_timestamp 
ON file_availability (timestamp DESC);

-- Indexes for irnss_data table (if they don't exist)
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_irnss_data_sat_mjd_sttime_source 
ON irnss_data (sat, mjd, sttime, source);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_irnss_data_source_mjd 
ON irnss_data (source, mjd);

-- Step 4: Optimize PostgreSQL settings for deadlock reduction
-- (These are suggestions - adjust based on your hardware and load)

-- Reduce deadlock timeout (faster deadlock detection)
-- ALTER SYSTEM SET deadlock_timeout = '100ms';

-- Increase shared buffers if possible (reduces I/O contention)
-- ALTER SYSTEM SET shared_buffers = '256MB';  -- Adjust based on available RAM

-- Enable better logging for deadlock analysis
-- ALTER SYSTEM SET log_lock_waits = on;
-- ALTER SYSTEM SET log_statement = 'ddl';

-- Step 5: Create a function to monitor deadlocks (optional)
CREATE OR REPLACE FUNCTION check_deadlocks()
RETURNS TABLE(
    deadlock_count bigint,
    last_deadlock_time timestamp
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        (SELECT setting::bigint FROM pg_stat_database WHERE datname = current_database()) as deadlock_count,
        (SELECT max(query_start) FROM pg_stat_activity WHERE state = 'idle') as last_deadlock_time;
END;
$$ LANGUAGE plpgsql;

-- Step 6: Verification queries
-- Check for remaining duplicates in file_availability
SELECT 'file_availability duplicates' as table_name, source, mjd, COUNT(*) as duplicate_count
FROM file_availability 
GROUP BY source, mjd 
HAVING COUNT(*) > 1

UNION ALL

-- Check for duplicates in irnss_data (if any)
SELECT 'irnss_data duplicates' as table_name, 
       CONCAT(sat, '-', mjd, '-', sttime, '-', source) as composite_key, 
       COUNT(*) as duplicate_count
FROM irnss_data 
GROUP BY sat, mjd, sttime, source 
HAVING COUNT(*) > 1;

-- Step 7: Monitor current locks (run this if deadlocks are still occurring)
-- SELECT 
--     blocked_locks.pid AS blocked_pid,
--     blocked_activity.usename AS blocked_user,
--     blocking_locks.pid AS blocking_pid,
--     blocking_activity.usename AS blocking_user,
--     blocked_activity.query AS blocked_statement,
--     blocking_activity.query AS current_statement_in_blocking_process
-- FROM pg_catalog.pg_locks blocked_locks
-- JOIN pg_catalog.pg_stat_activity blocked_activity ON blocked_activity.pid = blocked_locks.pid
-- JOIN pg_catalog.pg_locks blocking_locks 
--     ON blocking_locks.locktype = blocked_locks.locktype
--     AND blocking_locks.database IS NOT DISTINCT FROM blocked_locks.database
--     AND blocking_locks.relation IS NOT DISTINCT FROM blocked_locks.relation
--     AND blocking_locks.page IS NOT DISTINCT FROM blocked_locks.page
--     AND blocking_locks.tuple IS NOT DISTINCT FROM blocked_locks.tuple
--     AND blocking_locks.virtualxid IS NOT DISTINCT FROM blocked_locks.virtualxid
--     AND blocking_locks.transactionid IS NOT DISTINCT FROM blocked_locks.transactionid
--     AND blocking_locks.classid IS NOT DISTINCT FROM blocked_locks.classid
--     AND blocking_locks.objid IS NOT DISTINCT FROM blocked_locks.objid
--     AND blocking_locks.objsubid IS NOT DISTINCT FROM blocked_locks.objsubid
--     AND blocking_locks.pid != blocked_locks.pid
-- JOIN pg_catalog.pg_stat_activity blocking_activity ON blocking_activity.pid = blocking_locks.pid
-- WHERE NOT blocked_locks.granted;
