package com.time.tracealibility.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Database initialization component that runs on application startup
 * to ensure required unique indexes exist for materialized view concurrent refresh
 */
@Component
public class DatabaseInitializer implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);
    
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        logger.info("🔧 Running database initialization checks...");
        createMaterializedViewUniqueIndex();
        logger.info("✅ Database initialization completed successfully");
    }

    private void createMaterializedViewUniqueIndex() {
        try {
            // First check if the materialized view exists
            String checkViewSql = """
                SELECT COUNT(*) FROM information_schema.views 
                WHERE table_name = 'sat_common_view_difference_materialized' 
                AND table_schema = 'public'
                """;
            
            Integer viewCount = jdbcTemplate.queryForObject(checkViewSql, Integer.class);
            if (viewCount == null || viewCount == 0) {
                logger.warn("⚠️ Materialized view sat_common_view_difference_materialized not found, skipping index creation");
                return;
            }

            // Check if unique index already exists
            String checkIndexSql = """
                SELECT COUNT(*) FROM pg_indexes 
                WHERE tablename = 'sat_common_view_difference_materialized' 
                AND indexname LIKE '%unique%'
                AND indexdef LIKE '%UNIQUE%'
                """;
            
            Integer uniqueIndexCount = jdbcTemplate.queryForObject(checkIndexSql, Integer.class);
            if (uniqueIndexCount != null && uniqueIndexCount > 0) {
                logger.info("✅ Unique index already exists for materialized view");
                return;
            }

            logger.info("🔨 Creating unique index for materialized view concurrent refresh...");

            // Try to create unique index on 'id' column first (simplest approach)
            try {
                String createUniqueIndexSql = """
                    CREATE UNIQUE INDEX CONCURRENTLY IF NOT EXISTS idx_sat_common_view_diff_mat_unique_id
                    ON sat_common_view_difference_materialized (id)
                    """;
                
                jdbcTemplate.execute(createUniqueIndexSql);
                logger.info("✅ Successfully created unique index on 'id' column");
                
            } catch (Exception e) {
                logger.warn("⚠️ Failed to create unique index on 'id' column: " + e.getMessage());
                
                // Fallback: Try composite unique index
                try {
                    logger.info("🔨 Attempting to create composite unique index...");
                    String createCompositeIndexSql = """
                        CREATE UNIQUE INDEX CONCURRENTLY IF NOT EXISTS idx_sat_common_view_diff_mat_unique_composite
                        ON sat_common_view_difference_materialized (source2, mjd_date_time, satellite, reference_system, station)
                        """;
                    
                    jdbcTemplate.execute(createCompositeIndexSql);
                    logger.info("✅ Successfully created composite unique index");
                    
                } catch (Exception e2) {
                    logger.error("❌ Failed to create any unique index: " + e2.getMessage());
                    logger.info("💡 Materialized view will use non-concurrent refresh instead");
                }
            }

            // Update table statistics
            jdbcTemplate.execute("ANALYZE sat_common_view_difference_materialized");
            
        } catch (Exception e) {
            logger.error("❌ Error during database initialization: " + e.getMessage(), e);
        }
    }
}
