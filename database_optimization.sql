-- Database Performance Optimization for Time Traceability Application
-- Execute these commands in your PostgreSQL database for significant performance improvements

-- IMPORTANT: sat_common_view_difference is a VIEW, not a table!
-- We need to optimize the underlying base table: irnss_data_view (or its base table)

-- 1. First, let's find the base table for irnss_data_view
-- Check if irnss_data_view is also a view or a table
SELECT 
    table_name,
    table_type
FROM information_schema.tables 
WHERE table_name IN ('irnss_data_view', 'irnss_data')
ORDER BY table_name;

-- 2. Get the definition of irnss_data_view to find the real base table
SELECT pg_get_viewdef('irnss_data_view'::regclass);

-- 3. Essential Indexes for the BASE TABLE: irnss_data
-- These indexes will dramatically improve query performance for the views

-- Index for MJD (Modified Julian Date) - critical for date calculations
CREATE INDEX IF NOT EXISTS idx_irnss_data_mjd 
ON irnss_data(mjd);

-- Index for STTIME - used in date/time calculations  
CREATE INDEX IF NOT EXISTS idx_irnss_data_sttime 
ON irnss_data(sttime);

-- Index for source filtering (critical for joins in the view)
CREATE INDEX IF NOT EXISTS idx_irnss_data_source 
ON irnss_data(source);

-- Index for satellite ID (used in joins and filtering)
CREATE INDEX IF NOT EXISTS idx_irnss_data_sat_id 
ON irnss_data(sat_id);

-- Index for satellite (used in joins)
CREATE INDEX IF NOT EXISTS idx_irnss_data_sat 
ON irnss_data(sat);

-- Composite index for the main join condition in the view (sat + source)
CREATE INDEX IF NOT EXISTS idx_irnss_data_sat_source 
ON irnss_data(sat, source);

-- Composite index for date range queries (mjd + sttime for datetime calculations)
CREATE INDEX IF NOT EXISTS idx_irnss_data_datetime_calc 
ON irnss_data(mjd, sttime);

-- Composite index for complete filtering (most comprehensive)
CREATE INDEX IF NOT EXISTS idx_irnss_data_composite_full 
ON irnss_data(source, mjd, sttime, sat_id);

-- 4. Query Performance Statistics
-- Run this to see table statistics for the BASE table
SELECT 
    schemaname,
    tablename,
    attname,
    n_distinct,
    correlation
FROM pg_stats 
WHERE tablename IN ('irnss_data', 'irnss_data_view')
ORDER BY tablename, attname;

-- 5. Table Size Analysis for base table
SELECT 
    'irnss_data' as table_name,
    pg_size_pretty(pg_total_relation_size('irnss_data')) as "Total Size",
    pg_size_pretty(pg_relation_size('irnss_data')) as "Table Size",
    (SELECT count(*) FROM irnss_data) as "Row Count"
WHERE EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'irnss_data')

UNION ALL

SELECT 
    'irnss_data_view' as table_name,
    pg_size_pretty(pg_total_relation_size('irnss_data_view')) as "Total Size",
    pg_size_pretty(pg_relation_size('irnss_data_view')) as "Table Size", 
    (SELECT count(*) FROM irnss_data_view) as "Row Count"
WHERE EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'irnss_data_view');

-- 6. Index Usage Monitoring (run after implementing optimizations)
SELECT 
    indexrelname as index_name,
    idx_scan as times_used,
    idx_tup_read as tuples_read,
    idx_tup_fetch as tuples_fetched
FROM pg_stat_user_indexes 
WHERE schemaname = 'public' 
AND relname IN ('irnss_data', 'irnss_data_view')
ORDER BY relname, idx_scan DESC;

-- 7. Performance Testing Queries
-- Test these queries before and after index creation to measure improvement

-- Test 1: Base table query performance
EXPLAIN ANALYZE 
SELECT * FROM irnss_data 
WHERE source IN ('GZLI2P', 'IRNPLI') 
AND mjd_date_time >= '2024-01-01'::timestamp 
ORDER BY mjd_date_time DESC 
LIMIT 100;

-- Test 2: View query performance (your actual application query)
EXPLAIN ANALYZE 
SELECT * FROM sat_common_view_difference 
WHERE source2 IN ('FAR', 'BLR') 
AND mjd_date_time >= '2024-01-01'::timestamp 
ORDER BY mjd_date_time DESC 
LIMIT 100;

-- Test 3: Complex filter query on view
EXPLAIN ANALYZE 
SELECT * FROM sat_common_view_difference 
WHERE source2 IN ('FAR', 'BLR') 
AND mjd_date_time >= '2024-01-01'::timestamp 
AND mjd_date_time <= '2024-12-31'::timestamp 
AND sat_letter = 'GPS'
ORDER BY mjd_date_time DESC 
LIMIT 20;

-- 8. Create materialized views for high-performance queries
-- Note: These should match the views configured in your application.properties

-- Create materialized view for sat_common_view_difference
CREATE MATERIALIZED VIEW IF NOT EXISTS sat_common_view_difference_materialized AS
SELECT * FROM sat_common_view_difference;

-- Create materialized view for sat_pivoted_view  
CREATE MATERIALIZED VIEW IF NOT EXISTS sat_pivoted_view_materialized AS
SELECT * FROM sat_pivoted_view;

-- Create materialized view for sat_combined_view_difference
CREATE MATERIALIZED VIEW IF NOT EXISTS sat_combined_view_difference_materialized AS
SELECT * FROM sat_combined_view_difference;

-- 9. Create UNIQUE indexes on materialized views for CONCURRENT refresh support
-- PostgreSQL requires unique indexes to enable "REFRESH MATERIALIZED VIEW CONCURRENTLY"

-- Unique index for sat_common_view_difference_materialized
-- Using a combination of columns that should be unique per row
CREATE UNIQUE INDEX IF NOT EXISTS idx_sat_common_mat_unique 
ON sat_common_view_difference_materialized(id);

-- Unique index for sat_pivoted_view_materialized  
-- Using the ID column which should be unique per row
CREATE UNIQUE INDEX IF NOT EXISTS idx_sat_pivoted_mat_unique 
ON sat_pivoted_view_materialized(id);

-- Unique index for sat_combined_view_difference_materialized
-- Using the ID column which should be unique per row
CREATE UNIQUE INDEX IF NOT EXISTS idx_sat_combined_mat_unique 
ON sat_combined_view_difference_materialized(id);

-- 10. Create additional performance indexes on materialized views
CREATE INDEX IF NOT EXISTS idx_sat_common_mat_mjd_datetime 
ON sat_common_view_difference_materialized(mjd_date_time);

CREATE INDEX IF NOT EXISTS idx_sat_common_mat_source2 
ON sat_common_view_difference_materialized(source2);

CREATE INDEX IF NOT EXISTS idx_sat_pivoted_mat_mjd_datetime 
ON sat_pivoted_view_materialized(mjd_date_time);

CREATE INDEX IF NOT EXISTS idx_sat_combined_mat_mjd_datetime 
ON sat_combined_view_difference_materialized(mjd_date_time);

CREATE INDEX IF NOT EXISTS idx_sat_combined_mat_source2 
ON sat_combined_view_difference_materialized(source2);

-- 11. Analyze tables for better query planning
ANALYZE irnss_data;
ANALYZE irnss_data_view;
ANALYZE sat_common_view_difference_materialized;
ANALYZE sat_pivoted_view_materialized;
ANALYZE sat_combined_view_difference_materialized;

-- 12. Check existing indexes on base table
SELECT 
    indexname,
    indexdef
FROM pg_indexes 
WHERE tablename IN ('irnss_data', 'irnss_data_view')
ORDER BY tablename, indexname;
