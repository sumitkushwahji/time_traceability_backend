-- V1__baseline.sql
-- Baseline migration to acknowledge existing database schema created by Hasura
-- This migration establishes the starting point for Spring Boot Flyway migrations

-- The database already contains:
-- - All tables created by Hasura migrations
-- - Materialized view: sat_common_view_difference_materialized  
-- - Various indexes on the materialized view
-- 
-- This baseline migration doesn't need to create anything,
-- it just establishes version 1 as the starting point

SELECT 'Baseline established - existing schema acknowledged' AS status;
