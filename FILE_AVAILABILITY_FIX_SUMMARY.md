# File Availability Tracking - Duplicate Data Fix

## Problems Fixed

### 1. **Duplicate FileAvailability Records**
- **Root Cause**: Auto-generated ID in entity + parallel processing race conditions
- **Solution**: Added unique constraint and proper upsert logic

### 2. **Table Not Updating with New Files**
- **Root Cause**: Always creating new records instead of updating existing ones
- **Solution**: Implemented proper upsert method with update-first approach

## Changes Made

### 1. FileAvailability.java
- Added unique constraint on `(source, mjd)` combination
- This prevents database-level duplicates

### 2. FileAvailabilityRepository.java
- Added `updateFileAvailability()` method for efficient updates
- Uses native SQL UPDATE query for better performance

### 3. IrnssDataService.java
- Replaced direct `save()` calls with `upsertFileAvailability()` method
- Added proper transaction handling with `@Transactional`
- Removed `.parallel()` processing to reduce race conditions
- Added graceful exception handling for constraint violations

### 4. Database Migration
- Created SQL script to clean existing duplicates
- Adds necessary indexes for performance

## Migration Steps

1. **Run the SQL Migration**:
   ```bash
   psql -U your_username -d your_database -f database_migration_file_availability.sql
   ```

2. **Restart the Application**:
   - The new unique constraint will be automatically detected by Hibernate
   - New upsert logic will prevent future duplicates

## Expected Results

✅ **No More Duplicates**: Database constraint prevents duplicate (source, mjd) combinations

✅ **Proper Updates**: Existing file availability records will be updated instead of creating new ones

✅ **Better Performance**: Optimized queries and reduced race conditions

✅ **Graceful Error Handling**: Constraint violations are caught and logged without crashing

## Verification

After deployment, run this query to verify no duplicates exist:

```sql
SELECT source, mjd, COUNT(*) as count
FROM file_availability 
GROUP BY source, mjd 
HAVING COUNT(*) > 1;
```

This should return 0 rows.

## Performance Impact

- **Positive**: Faster lookups due to unique constraint index
- **Positive**: Fewer database writes (updates instead of inserts)
- **Positive**: Reduced parallel processing overhead
- **Minimal**: Slight overhead from constraint checking (negligible)
