package com.time.tracealibility.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "*")
public class TestController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "OK");
        response.put("timestamp", LocalDateTime.now());
        response.put("service", "Time Traceability Backend");
        response.put("version", "1.0.0");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/statistics/simple")
    public ResponseEntity<Map<String, Object>> simpleStatistics() {
        Map<String, Object> response = new HashMap<>();
        response.put("totalRecords", 12345);
        response.put("activeLocations", 6);
        response.put("satellites", 25);
        response.put("dataQuality", 94.5);
        response.put("lastUpdated", LocalDateTime.now());
        
        // Mock matrix data for testing
        Map<String, Map<String, Object>> mockMatrix = new HashMap<>();
        String[] locations = {"Bangalore", "Faridabad", "DRC", "Ahmedabad"};
        String[] refSystems = {"refsys1", "refsys2", "refsys3"};
        
        for (String location : locations) {
            Map<String, Object> locationData = new HashMap<>();
            for (String refSys : refSystems) {
                Map<String, Object> cellData = new HashMap<>();
                cellData.put("hasData", true);
                cellData.put("recordCount", (int)(Math.random() * 1000) + 100);
                cellData.put("satellites", new String[]{"G", "E", "R"});
                cellData.put("completeness", 85 + (Math.random() * 15));
                cellData.put("status", "AVAILABLE");
                locationData.put(refSys, cellData);
            }
            mockMatrix.put(location, locationData);
        }
        
        response.put("mockMatrix", mockMatrix);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/database/connection")
    public ResponseEntity<Map<String, Object>> testDatabaseConnection() {
        Map<String, Object> response = new HashMap<>();
        try {
            // This is a simple test - in reality you'd inject a DataSource or Repository
            response.put("database", "PostgreSQL");
            response.put("status", "Connected");
            response.put("timestamp", LocalDateTime.now());
        } catch (Exception e) {
            response.put("database", "PostgreSQL");
            response.put("status", "Error: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now());
        }
        return ResponseEntity.ok(response);
    }
}
