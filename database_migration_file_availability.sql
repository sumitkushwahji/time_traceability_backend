-- Migration script to fix File Availability duplicate data issues
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

-- Step 3: Create index for better performance on frequent lookups
CREATE INDEX IF NOT EXISTS idx_file_availability_source_mjd_status 
ON file_availability (source, mjd, status);

-- Step 4: Create index for time-based queries
CREATE INDEX IF NOT EXISTS idx_file_availability_timestamp 
ON file_availability (timestamp);

-- Verification query - should return 0 duplicates after migration
SELECT source, mjd, COUNT(*) as duplicate_count
FROM file_availability 
GROUP BY source, mjd 
HAVING COUNT(*) > 1;
