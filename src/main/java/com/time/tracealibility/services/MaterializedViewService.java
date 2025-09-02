package com.time.tracealibility.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MaterializedViewService {

  private static final Logger logger = LoggerFactory.getLogger(MaterializedViewService.class);

  @Autowired
  private JdbcTemplate jdbcTemplate;

  /**
   * Refreshes a single materialized view and analyzes it within a transaction.
   * @param viewName The name of the materialized view to refresh.
   * @throws IllegalStateException if the view does not exist.
   */
  @Transactional
  public void refreshView(String viewName) {
    logger.info("🚀 Starting refresh for view: {}", viewName);

    // 1. Check if materialized view exists
    if (!viewExists(viewName)) {
      String errorMessage = String.format("Materialized view '%s' does not exist! Skipping.", viewName);
      logger.error("❌ {}", errorMessage);
      throw new IllegalStateException(errorMessage);
    }

    // 2. Refresh the materialized view concurrently
    logger.debug("Executing REFRESH for {}", viewName);
    jdbcTemplate.execute("REFRESH MATERIALIZED VIEW CONCURRENTLY " + viewName);

    // 3. Update statistics for optimal query planning
    logger.debug("Executing ANALYZE for {}", viewName);
    jdbcTemplate.execute("ANALYZE " + viewName);

    logger.info("✅ Successfully refreshed and analyzed view: {}", viewName);
  }

  /**
   * Checks if a materialized view exists in the database.
   * @param viewName The name of the view to check.
   * @return true if the view exists, false otherwise.
   */
  private boolean viewExists(String viewName) {
    try {
      // Use a parameterized query to prevent any risk of SQL injection
      String sql = "SELECT COUNT(*) FROM pg_matviews WHERE matviewname = ?";
      Integer count = jdbcTemplate.queryForObject(sql, Integer.class, viewName);
      return count != null && count > 0;
    } catch (Exception e) {
      logger.error("Error checking for existence of view: {}", viewName, e);
      return false;
    }
  }
}
