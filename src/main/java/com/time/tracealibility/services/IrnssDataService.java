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

    // Run every 5 minutes
    @Scheduled(fixedRate = 300_000)
    public void monitorLocationFolders() throws IOException {
        Files.list(Paths.get(parentFolder))
                .filter(Files::isDirectory)
                .forEach(this::processLocationFolder);
    }

    private void processLocationFolder(Path locationFolder) {
        try {
            String source = locationFolder.getFileName().toString().toUpperCase(); // derive source

            Set<Integer> foundMjdSet = new HashSet<>();

            List<Path> allFiles = Files.list(locationFolder)
                    .filter(Files::isRegularFile)
                    .collect(Collectors.toList());

            for (Path filePath : allFiles) {
                FileInfo fileInfo = extractSourceAndMJD(filePath.getFileName().toString());
                if (fileInfo != null) {
                    foundMjdSet.add(fileInfo.mjd);

                    // Save as available
                    fileAvailabilityRepository.save(
                            new FileAvailability(null, source, fileInfo.mjd, "AVAILABLE",
                                    filePath.getFileName().toString(), LocalDateTime.now())
                    );

                    processLiveFile(filePath, fileInfo.source, fileInfo.mjd);
                }
            }

            // Check for missing MJDs in expected range
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

        if (lines.size() <= 19) return;

        String fileKey = filePath.toAbsolutePath().toString();
        int lastProcessed = processedFileRepository.findById(fileKey)
                .map(ProcessedFile::getLastLineProcessed)
                .orElse(19); // Skip headers

        int currentLine = lastProcessed;

        for (int i = lastProcessed; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty() || !Character.isDigit(line.charAt(0))) continue;

            String[] tokens = line.split("\\s+");
            if (tokens.length < 25) continue;

            IrnssData data = new IrnssData();
            data.setSat(Integer.parseInt(tokens[0]));
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
            data.setIonType(tokens[24]);
            data.setSource(source); // ✔ from filename
            irnssDataRepository.save(data);

            currentLine = i + 1;
        }

        if (currentLine > lastProcessed) {
            processedFileRepository.save(new ProcessedFile(fileKey, currentLine));
        }
    }

    private int parseSignedInt(String str) {
        return Integer.parseInt(str.replace("+", ""));
    }

    private int extractMjdFromFilename(Path path) {
        FileInfo info = extractSourceAndMJD(path.getFileName().toString());
        return (info != null) ? info.mjd : 0;
    }

    // ✔ Correct source/MJD extraction from filename
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

    // Optional DTO for source & MJD
    private static class FileInfo {
        String source;
        int mjd;

        FileInfo(String source, int mjd) {
            this.source = source;
            this.mjd = mjd;
        }
    }

    // Public methods for dashboard/reporting
    public List<SourceSessionStatusDTO> getSessionCompleteness(String source, String mjd, Integer currentSessionCount, Integer expectedSessionCount) {
        return irnssDataRepository.findSessionCountsByFilters(source, mjd, currentSessionCount, expectedSessionCount);
    }

    public List<SourceSessionStatusDTO> getSessionCompleteness(String mjd) {
        return irnssDataRepository.findSessionCountsByMjd(mjd);
    }
}
