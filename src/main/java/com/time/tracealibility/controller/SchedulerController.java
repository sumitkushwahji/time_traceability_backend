package com.time.tracealibility.controller;


import com.time.tracealibility.scheduler.MaterializedViewScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/scheduler")
@CrossOrigin(origins = "*") // Configure for your specific needs in production
public class SchedulerController {

  @Autowired
  private MaterializedViewScheduler materializedViewScheduler;

  /**
   * Gets the current status of all managed materialized views.
   */
  @GetMapping("/status")
  public ResponseEntity<Map<String, Map<String, Object>>> getStatus() {
    Map<String, Map<String, Object>> status = materializedViewScheduler.getSchedulerStatus();
    return ResponseEntity.ok(status);
  }

  /**
   * Manually triggers a refresh for all materialized views.
   * The refresh runs in the background.
   */
  @PostMapping("/refresh")
  public ResponseEntity<Map<String, String>> manualRefresh() {
    materializedViewScheduler.manualRefresh();
    return ResponseEntity.accepted().body(Map.of(
      "status", "success",
      "message", "Manual refresh job triggered for all views. Check /status for progress."
    ));
  }

  /**
   * Provides a high-level health check of the refresh system.
   * It's healthy if no views have a 'Failed' status.
   */
  @GetMapping("/health")
  public ResponseEntity<Map<String, Object>> getHealth() {
    Map<String, Map<String, Object>> statuses = materializedViewScheduler.getSchedulerStatus();

    boolean isOverallHealthy = statuses.values().stream()
      .noneMatch(viewStatus -> "Failed".equals(viewStatus.get("lastRefreshStatus")));

    Map<String, Object> healthResponse = new HashMap<>();
    healthResponse.put("overallStatus", isOverallHealthy ? "HEALTHY" : "UNHEALTHY");

    // Provide a summary of each view's status for quick diagnostics
    Map<String, String> viewSummary = statuses.entrySet().stream()
      .collect(Collectors.toMap(
        Map.Entry::getKey,
        entry -> (String) entry.getValue().getOrDefault("lastRefreshStatus", "Pending")
      ));

    healthResponse.put("views", viewSummary);

    return ResponseEntity.ok(healthResponse);
  }
}
