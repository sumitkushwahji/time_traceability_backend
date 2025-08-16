package com.time.tracealibility.controller;

import com.time.tracealibility.scheduler.MaterializedViewScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller for monitoring and controlling the materialized view scheduler
 */
@RestController
@RequestMapping("/api/scheduler")
@CrossOrigin(origins = "*")
public class SchedulerController {

    @Autowired
    private MaterializedViewScheduler materializedViewScheduler;

    /**
     * Get current scheduler status
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> status = materializedViewScheduler.getSchedulerStatus();
        return ResponseEntity.ok(status);
    }

    /**
     * Manually trigger materialized view refresh
     */
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> manualRefresh() {
        try {
            materializedViewScheduler.manualRefresh();
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Manual refresh triggered successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", "Failed to trigger refresh: " + e.getMessage()
            ));
        }
    }

    /**
     * Get scheduler health check
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getHealth() {
        Map<String, Object> status = materializedViewScheduler.getSchedulerStatus();
        
        boolean isHealthy = !"Failed".startsWith((String) status.get("lastRefreshStatus"));
        
        return ResponseEntity.ok(Map.of(
            "healthy", isHealthy,
            "lastRefreshStatus", status.get("lastRefreshStatus"),
            "lastRefreshTime", status.get("lastRefreshTime"),
            "isCurrentlyRefreshing", status.get("isRefreshing")
        ));
    }
}
