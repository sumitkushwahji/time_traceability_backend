package com.time.tracealibility.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Startup configuration to verify materialized view exists and is ready
 */
@Component
public class MaterializedViewStartupCheck implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(MaterializedViewStartupCheck.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        logger.info("🔍 Checking materialized view configuration...");
        
        try {
            // Check if materialized view exists
            String checkViewSql = "SELECT COUNT(*) FROM pg_matviews WHERE matviewname = 'sat_common_view_difference_materialized'";
            Integer viewCount = jdbcTemplate.queryForObject(checkViewSql, Integer.class);
            
            if (viewCount == null || viewCount == 0) {
                logger.error("❌ Materialized view 'sat_common_view_difference_materialized' not found!");
                logger.error("🔧 Please run the Hasura migration to create the materialized view");
                return;
            }
            
            logger.info("✅ Materialized view found");
            
            // Check row count
            String countSql = "SELECT COUNT(*) FROM sat_common_view_difference_materialized";
            Integer rowCount = jdbcTemplate.queryForObject(countSql, Integer.class);
            logger.info("📊 Materialized view contains {} rows", rowCount);
            
            // Check indexes
            String indexSql = "SELECT COUNT(*) FROM pg_indexes WHERE tablename = 'sat_common_view_difference_materialized'";
            Integer indexCount = jdbcTemplate.queryForObject(indexSql, Integer.class);
            logger.info("🗂️  Found {} indexes on materialized view", indexCount);
            
            // Performance test
            long startTime = System.nanoTime();
            String testSql = "SELECT COUNT(*) FROM sat_common_view_difference_materialized WHERE source2 = 'BLR'";
            Integer testCount = jdbcTemplate.queryForObject(testSql, Integer.class);
            long endTime = System.nanoTime();
            
            double durationMs = (endTime - startTime) / 1_000_000.0;
            logger.info("🚀 Performance test: {} rows found in {:.3f} ms", testCount, durationMs);
            
            if (durationMs < 10.0) {
                logger.info("🎉 Performance optimization is working perfectly!");
            } else {
                logger.warn("⚠️ Performance could be improved - check if indexes are in place");
            }
            
            logger.info("✅ Materialized view startup check completed successfully");
            
        } catch (Exception e) {
            logger.error("❌ Error during materialized view startup check", e);
        }
    }
}
