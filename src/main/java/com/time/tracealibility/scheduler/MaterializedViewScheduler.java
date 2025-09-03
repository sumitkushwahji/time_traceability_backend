package com.time.tracealibility.scheduler;


import com.time.tracealibility.services.MaterializedViewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ConditionalOnProperty(value = "app.scheduler.materialized-view.enabled", havingValue = "true", matchIfMissing = true)
public class MaterializedViewScheduler {

  private static final Logger logger = LoggerFactory.getLogger(MaterializedViewScheduler.class);

  @Autowired
  private MaterializedViewService materializedViewService;

  // Inject the list of view names using SpEL to split comma-separated values
  @Value("#{'${app.scheduler.materialized-view.names:}'.split(',')}")
  private List<String> viewNames;

  // Use a thread-safe Map to store the status of each view
  private final Map<String, Map<String, Object>> statuses = new ConcurrentHashMap<>();
  private volatile boolean isJobRunning = false;

  @PostConstruct
  public void init() {
    logger.info("🔧 MaterializedViewScheduler initialized");
    logger.info("📝 Raw viewNames from SpEL: {}", viewNames);
    logger.info("📝 ViewNames size: {}", viewNames != null ? viewNames.size() : "null");
    
    if (viewNames != null && !viewNames.isEmpty()) {
      for (int i = 0; i < viewNames.size(); i++) {
        String viewName = viewNames.get(i).trim();
        logger.info("📝 View[{}]: '{}' (length: {})", i, viewName, viewName.length());
      }
    } else {
      logger.error("❌ No view names loaded! ViewNames: {}", viewNames);
    }
  }

  @Scheduled(fixedRateString = "${app.scheduler.materialized-view.refresh-interval}")
  public void refreshAllMaterializedViews() {
    if (isJobRunning) {
      logger.warn("Previous refresh job still in progress. Skipping this cycle.");
      return;
    }

    // Filter out empty view names that might result from splitting empty strings
    List<String> validViewNames = viewNames.stream()
        .map(String::trim)
        .filter(name -> !name.isEmpty())
        .toList();

    if (validViewNames.isEmpty()) {
      logger.warn("No materialized views configured for refresh. Skipping job.");
      return;
    }

    isJobRunning = true;
    logger.info("=============== 🔄 Starting Materialized View Refresh Job for {} views ===============", validViewNames.size());

    for (String viewName : validViewNames) {
      long startTime = System.currentTimeMillis();
      try {
        materializedViewService.refreshView(viewName);
        long duration = System.currentTimeMillis() - startTime;
        updateStatus(viewName, "Success", duration, null);
      } catch (Exception e) {
        long duration = System.currentTimeMillis() - startTime;
        logger.error("❌ Failed to refresh view '{}' after {} ms", viewName, duration, e);
        updateStatus(viewName, "Failed", duration, e.getMessage());
      }
    }

    logger.info("=============== ✅ Materialized View Refresh Job Finished ===============\n");
    isJobRunning = false;
  }

  private void updateStatus(String viewName, String status, long duration, String errorMessage) {
    Map<String, Object> viewStatus = new ConcurrentHashMap<>();
    viewStatus.put("lastRefreshStatus", status);
    viewStatus.put("lastRefreshDurationMs", duration);
    viewStatus.put("lastRefreshTimestamp", System.currentTimeMillis());
    if (errorMessage != null) {
      viewStatus.put("error", errorMessage);
    }
    statuses.put(viewName, viewStatus);
  }

  /**
   * Get the status of all managed views for monitoring.
   */
  public Map<String, Map<String, Object>> getSchedulerStatus() {
    return statuses;
  }

  /**
   * Manual refresh endpoint for testing or immediate updates.
   */
  public void manualRefresh() {
    logger.info("🔧 Manual refresh triggered for all views.");
    // Run in a new thread to avoid blocking the HTTP request
    new Thread(this::refreshAllMaterializedViews).start();
  }
}
