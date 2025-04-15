// FileScheduler.java
package com.time.tracealibility.scheduler;

import com.time.tracealibility.services.IrnssDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class FileScheduler {

    @Autowired
    private IrnssDataService irnssDataService;

    @Scheduled(fixedRate = 20000)
    public void runScheduledTask() {
        try {
            System.out.println("Running IRNSS file parser...");
            irnssDataService.processFiles();
            System.out.println("Parsing completed.");
        } catch (Exception e) {
            System.err.println("Scheduled Task Error: " + e.getMessage());
        }
    }
}
