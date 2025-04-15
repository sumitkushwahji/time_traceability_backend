package com.time.tracealibility.controllers;

import com.time.tracealibility.services.IrnssDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/irnss")
public class IrnssDataController {

    @Autowired
    private IrnssDataService service;

    @PostMapping("/process")
    public ResponseEntity<String> processFiles() {
        try {
            service.processFiles();
            return ResponseEntity.ok("Files processed successfully.");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to process files.");
        }
    }

}
