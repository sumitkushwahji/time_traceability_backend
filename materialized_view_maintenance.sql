-- Materialized View Maintenance Script
-- Run this periodically to keep the materialized view up-to-date

-- 1. Refresh the materialized view with new data
REFRESH MATERIALIZED VIEW CONCURRENTLY sat_common_view_difference_materialized;

-- 2. Update table statistics for optimal query planning
ANALYZE sat_common_view_difference_materialized;

-- 3. Check the size and row count
SELECT 
    pg_size_pretty(pg_total_relation_size('sat_common_view_difference_materialized')) as "Total Size",
    pg_size_pretty(pg_relation_size('sat_common_view_difference_materialized')) as "Table Size",
    (SELECT count(*) FROM sat_common_view_difference_materialized) as "Row Count";

-- 4. Monitor index usage
SELECT 
    indexrelname as index_name,
    idx_scan as times_used,
    idx_tup_read as tuples_read,
    idx_tup_fetch as tuples_fetched
FROM pg_stat_user_indexes 
WHERE relname = 'sat_common_view_difference_materialized'
ORDER BY idx_scan DESC;

-- 5. Performance test after refresh
EXPLAIN ANALYZE 
SELECT * FROM sat_common_view_difference_materialized 
WHERE source2 IN ('FAR', 'BLR') 
AND mjd_date_time >= CURRENT_DATE - INTERVAL '30 days'
ORDER BY mjd_date_time DESC 
LIMIT 100;
