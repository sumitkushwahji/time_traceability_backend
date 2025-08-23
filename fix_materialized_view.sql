-- Fix for materialized view concurrent refresh
-- Create unique index required for concurrent refresh

-- First, check if there are any duplicate rows that would prevent unique index creation
SELECT source1, source2, sat_letter, mjd_datetime, COUNT(*)
FROM sat_common_view_difference_materialized
GROUP BY source1, source2, sat_letter, mjd_datetime
HAVING COUNT(*) > 1;

-- Create unique index on the materialized view
-- This allows concurrent refresh which is non-blocking
CREATE UNIQUE INDEX CONCURRENTLY IF NOT EXISTS idx_sat_common_view_diff_mat_unique 
ON sat_common_view_difference_materialized (source1, source2, sat_letter, mjd_datetime);

-- Alternative: If duplicates exist, create a unique index with a combination that ensures uniqueness
-- CREATE UNIQUE INDEX CONCURRENTLY IF NOT EXISTS idx_sat_common_view_diff_mat_unique 
-- ON sat_common_view_difference_materialized (id); -- assuming there's an id column

-- Verify the index was created
SELECT indexname, indexdef 
FROM pg_indexes 
WHERE tablename = 'sat_common_view_difference_materialized';

-- Test the concurrent refresh
REFRESH MATERIALIZED VIEW CONCURRENTLY sat_common_view_difference_materialized;
