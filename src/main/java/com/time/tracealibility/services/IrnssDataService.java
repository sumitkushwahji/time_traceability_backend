package com.time.tracealibility.services;

import com.time.tracealibility.dto.SourceSessionStatusDTO;
import com.time.tracealibility.entity.FileAvailability;
import com.time.tracealibility.entity.IrnssData;
import com.time.tracealibility.entity.ProcessedFile;
import com.time.tracealibility.repository.FileAvailabilityRepository;
import com.time.tracealibility.repository.IrnssDataRepository;
import com.time.tracealibility.repository.ProcessedFileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class IrnssDataService {

    @Value("${irnss.parent-folder}")
    private String parentFolder;

    @Autowired
    private IrnssDataRepository irnssDataRepository;

    @Autowired
    private ProcessedFileRepository processedFileRepository;

    @Autowired
    private FileAvailabilityRepository fileAvailabilityRepository;

    @Scheduled(fixedRate = 300_000) // 5 minutes
    public void monitorLocationFolders() {
        try {
            long startTime = System.currentTimeMillis();
            System.out.println("🔍 Starting file monitoring at: " + LocalDateTime.now());

            Files.list(Paths.get(parentFolder))
                    .filter(Files::isDirectory)
                    .forEach(this::processLocationFolder); // Remove .parallel() to reduce race conditions

            long duration = System.currentTimeMillis() - startTime;
            System.out.println("✅ File monitoring completed in: " + duration + "ms");
        } catch (IOException e) {
            System.err.println("❌ Error during file monitoring: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void processLocationFolder(Path locationFolder) {
        try {
            String folderName = locationFolder.getFileName().toString().toUpperCase();
            Set<Integer> foundMjdSet = new HashSet<>();
            Map<String, Set<Integer>> sourceToMjdMap = new HashMap<>(); // Track MJDs by source

            List<Path> allFiles = Files.walk(locationFolder)
                    .filter(Files::isRegularFile)
                    .filter(path -> {
                        // Filter out problematic files early
                        try {
                            return Files.size(path) > 0; // Skip empty files
                        } catch (IOException e) {
                            System.err.println("Cannot read file size for: " + path + " - " + e.getMessage());
                            return false;
                        }
                    })
                    .collect(Collectors.toList());

            System.out.println("Processing " + allFiles.size() + " files in folder: " + folderName);

            for (Path filePath : allFiles) {
                try {
                    FileInfo fileInfo = extractSourceAndMJD(filePath.getFileName().toString());
                    if (fileInfo != null) {

                      LocalDateTime creationTime = null;
                      try {
                        BasicFileAttributes attrs = Files.readAttributes(filePath, BasicFileAttributes.class);
                        creationTime = LocalDateTime.ofInstant(attrs.creationTime().toInstant(), ZoneId.systemDefault());
                      } catch (IOException e) {
                        System.err.println("Could not read creation time for: " + filePath.getFileName());
                      }

                      foundMjdSet.add(fileInfo.mjd);

                        // Track MJDs by source for missing file detection
                        sourceToMjdMap.computeIfAbsent(fileInfo.source, k -> new HashSet<>()).add(fileInfo.mjd);

                        // Use source from filename, not folder name - Use upsert logic
                      upsertFileAvailability(fileInfo.source, fileInfo.mjd, "AVAILABLE",
                        filePath.getFileName().toString(),
                        creationTime,
                        LocalDateTime.now()); // <-- UPDATE THIS LINE

                      processLiveFile(filePath, fileInfo.source, fileInfo.mjd);
                    }
                } catch (Exception e) {
                    System.err.println("Error processing file " + filePath.getFileName() + ": " + e.getMessage());
                    // Continue processing other files
                }
            }

            // Missing file detection logic - check for each source found in files
          // Missing file detection logic - check for each source found in files
          int todayMjd = (int) ChronoUnit.DAYS.between(LocalDate.of(1858, 11, 17), LocalDate.now());
          for (String source : sourceToMjdMap.keySet()) {
            Set<Integer> sourceMjds = sourceToMjdMap.get(source);
            for (int i = todayMjd - 3; i <= todayMjd; i++) {
              if (!sourceMjds.contains(i)) {
                Optional<FileAvailability> existing = fileAvailabilityRepository.findBySourceAndMjd(source, i);
                if (existing.isEmpty()) {
                  // FIX: Pass null for creationTime and LocalDateTime.now() for the last checked time.
                  upsertFileAvailability(source, i, "MISSING", null, null, LocalDateTime.now());
                }
              }
            }
          }

        } catch (IOException e) {
            System.err.println("Error processing location folder " + locationFolder + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void processLiveFile(Path filePath, String source, int mjd) throws IOException {
        List<String> lines;
        try {
            // Try UTF-8 first
            lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);
        } catch (java.nio.charset.MalformedInputException e) {
            System.err.println("UTF-8 encoding failed for " + filePath.getFileName() + ", trying ISO-8859-1");
            try {
                // Fallback to ISO-8859-1 (Latin-1) which can read any byte sequence
                lines = Files.readAllLines(filePath, StandardCharsets.ISO_8859_1);
            } catch (Exception e2) {
                System.err.println("All encoding attempts failed for " + filePath.getFileName() + ": " + e2.getMessage());
                return;
            }
        } catch (Exception e) {
            System.err.println("Error reading file " + filePath.getFileName() + ": " + e.getMessage());
            return;
        }

        if (lines.size() <= 20) {
            System.out.println("File too short (no data): " + filePath.getFileName());
            return;
        }

        int dataStartIndex = 20;

        String fileKey = filePath.toAbsolutePath().toString();
        int lastProcessed = processedFileRepository.findById(fileKey)
                .map(ProcessedFile::getLastLineProcessed)
                .orElse(dataStartIndex);

        int currentLine = lastProcessed;
        int insertedCount = 0;
        int skippedCount = 0;

        System.out.println("Processing file: " + filePath.getFileName());

        for (int i = lastProcessed; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) continue;

            String[] tokens = line.split("\\s+");
            if (tokens.length < 24) {
                System.err.println("Line " + i + " has insufficient tokens (" + tokens.length + "): " + line);
                skippedCount++;
                continue;
            }

            try {
                IrnssData data = parseLineToIrnssData(tokens, source);

                // Use deadlock-resistant save operation
                saveIrnssDataSafely(data);
                insertedCount++;

            } catch (NumberFormatException e) {
                System.err.println("Number format error on line " + i + " in " + filePath.getFileName() + ": " + e.getMessage());
                skippedCount++;
            } catch (Exception e) {
                System.err.println("Error parsing line " + i + " in " + filePath.getFileName() + ": " + line);
                System.err.println("Error details: " + e.getMessage());
                skippedCount++;
            }

            currentLine = i + 1;
        }

        if (currentLine > lastProcessed) {
            processedFileRepository.save(new ProcessedFile(fileKey, currentLine));
        }

        System.out.printf("Finished file: %s | Inserted: %d | Skipped: %d | Last Line: %d%n",
                filePath.getFileName(), insertedCount, skippedCount, currentLine);
    }

    private int parseSignedInt(String str) {
        if (str == null || str.isBlank() || str.equalsIgnoreCase("nan")) {
            return 0;
        }
        try {
            return Integer.parseInt(str.replace("+", ""));
        } catch (NumberFormatException e) {
            System.err.println("Unable to parse signed integer from: '" + str + "'");
            return 0;
        }
    }

    /**
     * Extract line parsing logic to separate method for better error handling
     */
    private IrnssData parseLineToIrnssData(String[] tokens, String source) {
        IrnssData data = new IrnssData();

        // SAT ID handling: G01, R02, or numeric
        String satToken = tokens[0];
        data.setSat(satToken.matches("[A-Z]\\d{2}") ? Integer.parseInt(satToken.substring(1)) : Integer.parseInt(satToken));
        data.setSatId(tokens[0]);
        data.setCl(tokens[1]);
        data.setMjd(Integer.parseInt(tokens[2]));
        data.setSttime(tokens[3]);
        data.setTrkl(Integer.parseInt(tokens[4]));
        data.setElv(Integer.parseInt(tokens[5]));
        data.setAzth(Integer.parseInt(tokens[6]));
        data.setRefsv(parseSignedInt(tokens[7]));
        data.setSrsv(parseSignedInt(tokens[8]));
        data.setRefsys(parseSignedInt(tokens[9]));
        data.setSrsys(parseSignedInt(tokens[10]));
        data.setDsg(Integer.parseInt(tokens[11]));
        data.setIoe(Integer.parseInt(tokens[12]));
        data.setMdtr(Integer.parseInt(tokens[13]));
        data.setSmdt(Integer.parseInt(tokens[14]));
        data.setMdio(Integer.parseInt(tokens[15]));
        data.setSmdi(Integer.parseInt(tokens[16]));
        data.setMsio(Integer.parseInt(tokens[17]));
        data.setSmsi(Integer.parseInt(tokens[18]));
        data.setIsg(Integer.parseInt(tokens[19]));
        data.setFr(Integer.parseInt(tokens[20]));
        data.setHc(Integer.parseInt(tokens[21]));
        data.setFrc(tokens[22]);
        data.setCk(tokens[23]);

        if (tokens.length >= 25) {
            data.setIonType(tokens[24]);
        }

        data.setSource(source);
        return data;
    }

    private FileInfo extractSourceAndMJD(String filename) {
        if (filename.length() < 7) return null;

        // Extract source (first 6 characters)
        String source = filename.substring(0, 6);

        // For files like GZLI2P60.866, we need to extract 60866 as MJD
        // Look for the pattern after the source: number.number
        String remainingPart = filename.substring(6);

        // Remove all non-digits and concatenate them
        String allDigits = remainingPart.replaceAll("[^0-9]", "");

        try {
            if (!allDigits.isEmpty()) {
                int mjd = Integer.parseInt(allDigits);
                return new FileInfo(source, mjd);
            }
        } catch (NumberFormatException e) {
            System.err.println("Error parsing MJD from filename: " + filename);
        }

        return null;
    }

    private static class FileInfo {
        String source;
        int mjd;

        FileInfo(String source, int mjd) {
            this.source = source;
            this.mjd = mjd;
        }
    }

    /**
     * Deadlock-resistant save method for IrnssData
     */
    private void saveIrnssDataSafely(IrnssData data) {
        try {
            // Check if record exists first (faster than catching exceptions)
            if (!irnssDataRepository.existsBySatAndMjdAndSttimeAndSource(
                    data.getSat(), data.getMjd(), data.getSttime(), data.getSource())) {
                irnssDataRepository.save(data);
            }
        } catch (Exception e) {
            String errorMessage = e.getMessage().toLowerCase();

            // If it's a constraint violation (duplicate), that's expected - silently ignore
            if (errorMessage.contains("duplicate") || errorMessage.contains("unique constraint")) {
                // This is fine - record already exists
                return;
            }

            // If it's a deadlock, log it but don't fail the entire process
            if (errorMessage.contains("deadlock") || errorMessage.contains("could not serialize access")) {
                System.err.println("Deadlock in IrnssData save for sat: " + data.getSat() + ", mjd: " + data.getMjd() + ", sttime: " + data.getSttime());
                return;
            }

            // For other errors, log and continue
            System.err.println("Error saving IrnssData: " + e.getMessage());
        }
    }

    /**
     * Deadlock-resistant upsert method using PostgreSQL native ON CONFLICT
     * This prevents deadlocks by using a single atomic operation
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    private void upsertFileAvailability(String source, int mjd, String status, String fileName, LocalDateTime fileCreationTime, LocalDateTime lastCheckedTimestamp) {

      int maxRetries = 3;
        int retryCount = 0;

        while (retryCount < maxRetries) {
            try {
                // Use PostgreSQL native UPSERT with ON CONFLICT - atomic operation prevents deadlocks
              fileAvailabilityRepository.upsertFileAvailability(
                source, mjd, status, fileName,
                fileCreationTime,
                lastCheckedTimestamp
              );
              return; // Success, exit retry loop

            } catch (Exception e) {
                String errorMessage = e.getMessage().toLowerCase();

                // Check if it's a deadlock-related error
                if (errorMessage.contains("deadlock") || errorMessage.contains("could not serialize access")) {
                    retryCount++;
                    System.err.println("Database contention detected (attempt " + retryCount + "/" + maxRetries + ") for source: " + source + ", mjd: " + mjd);

                    if (retryCount >= maxRetries) {
                        System.err.println("Max retries reached for file availability upsert: " + source + ", mjd: " + mjd);
                        return; // Give up after max retries
                    }

                    // Exponential backoff with jitter to reduce collision probability
                    try {
                        long delayMs = (long) (Math.pow(2, retryCount) * 50 + Math.random() * 50);
                        Thread.sleep(delayMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return;
                    }

                } else {
                    // Non-deadlock error, log and exit
                    System.err.println("Error in file availability upsert for source: " + source + ", mjd: " + mjd + " - " + e.getMessage());
                    return;
                }
            }
        }
    }

    public List<SourceSessionStatusDTO> getSessionCompleteness(String source, String mjd, Integer currentSessionCount, Integer expectedSessionCount) {
        return irnssDataRepository.findSessionCountsByFilters(source, mjd, currentSessionCount, expectedSessionCount);
    }

    public List<SourceSessionStatusDTO> getSessionCompleteness(String mjd) {
        return irnssDataRepository.findSessionCountsByMjd(mjd);
    }
}
