-- Add unique index for concurrent materialized view refresh
-- This index is required for PostgreSQL to perform concurrent refresh operations

-- First, let's check what columns are available and create a simple unique index
-- Based on the Hasura schema, the 'id' column should be unique as it's generated from source2 and mjd_date_time

-- Drop the index if it already exists (for re-running the migration)
DROP INDEX IF EXISTS idx_sat_common_view_diff_mat_unique;
DROP INDEX IF EXISTS idx_sat_common_view_diff_mat_unique_id;
DROP INDEX IF EXISTS idx_sat_common_view_diff_mat_unique_composite;

-- Create a unique index on the id column which should be unique
-- The id is generated as concat(source2, '-', mjd_date_time) in the view
CREATE UNIQUE INDEX idx_sat_common_view_diff_mat_unique_id
ON sat_common_view_difference_materialized (id);

-- Update table statistics after index creation
ANALYZE sat_common_view_difference_materialized;
