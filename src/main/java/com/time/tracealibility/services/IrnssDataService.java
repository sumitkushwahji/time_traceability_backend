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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

    @Scheduled(fixedRate = 300_000)
    public void monitorLocationFolders() throws IOException {
        Files.list(Paths.get(parentFolder))
                .filter(Files::isDirectory)
                .forEach(this::processLocationFolder);
    }

    private void processLocationFolder(Path locationFolder) {
        try {
            String source = locationFolder.getFileName().toString().toUpperCase();
            Set<Integer> foundMjdSet = new HashSet<>();

            List<Path> allFiles = Files.walk(locationFolder)
                    .filter(Files::isRegularFile)
                    .collect(Collectors.toList());

            for (Path filePath : allFiles) {
                FileInfo fileInfo = extractSourceAndMJD(filePath.getFileName().toString());
                if (fileInfo != null) {
                    foundMjdSet.add(fileInfo.mjd);
                    fileAvailabilityRepository.save(
                            new FileAvailability(null, source, fileInfo.mjd, "AVAILABLE",
                                    filePath.getFileName().toString(), LocalDateTime.now())
                    );
                    processLiveFile(filePath, fileInfo.source, fileInfo.mjd);
                }
            }

            int todayMjd = (int) ChronoUnit.DAYS.between(LocalDate.of(1858, 11, 17), LocalDate.now());
            for (int i = todayMjd - 3; i <= todayMjd; i++) {
                if (!foundMjdSet.contains(i)) {
                    Optional<FileAvailability> existing = fileAvailabilityRepository.findBySourceAndMjd(source, i);
                    if (existing.isEmpty()) {
                        fileAvailabilityRepository.save(
                                new FileAvailability(null, source, i, "MISSING", null, LocalDateTime.now())
                        );
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processLiveFile(Path filePath, String source, int mjd) throws IOException {
        List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);

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
                skippedCount++;
                continue;
            }

            try {
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

                if (!irnssDataRepository.existsBySatAndMjdAndSttimeAndSource(
                        data.getSat(), data.getMjd(), data.getSttime(), data.getSource())) {
                    irnssDataRepository.save(data);
                    insertedCount++;
                } else {
                    skippedCount++;
                }

            } catch (Exception e) {
                System.err.println("Error parsing line " + i + " in " + filePath.getFileName() + ": " + line);
                e.printStackTrace();
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

    private FileInfo extractSourceAndMJD(String filename) {
        if (filename.length() < 7) return null;
        String source = filename.substring(0, 6);
        String mjdPart = filename.replaceAll("[^0-9]", " ");
        Scanner scanner = new Scanner(mjdPart);
        if (scanner.hasNextInt()) {
            int mjd = scanner.nextInt();
            return new FileInfo(source, mjd);
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

    public List<SourceSessionStatusDTO> getSessionCompleteness(String source, String mjd, Integer currentSessionCount, Integer expectedSessionCount) {
        return irnssDataRepository.findSessionCountsByFilters(source, mjd, currentSessionCount, expectedSessionCount);
    }

    public List<SourceSessionStatusDTO> getSessionCompleteness(String mjd) {
        return irnssDataRepository.findSessionCountsByMjd(mjd);
    }
}
