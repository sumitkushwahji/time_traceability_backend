package com.time.tracealibility.services;

import com.time.tracealibility.dto.SourceSessionStatusDTO;
import com.time.tracealibility.entity.IrnssData;
import com.time.tracealibility.entity.ProcessedFile;
import com.time.tracealibility.repository.IrnssDataRepository;
import com.time.tracealibility.repository.ProcessedFileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class IrnssDataService {

    @Value("${irnss.parent-folder}")
    private String parentFolder;

    @Autowired
    private IrnssDataRepository repository;

    @Autowired
    private ProcessedFileRepository processedFileRepository;

    // Run every 5 minutes
    @Scheduled(fixedRate = 300_000)
    public void monitorLocationFolders() throws IOException {
        Files.list(Paths.get(parentFolder))
                .filter(Files::isDirectory)
                .forEach(this::processLocationFolder);
    }

    private void processLocationFolder(Path locationFolder) {
        try {
            List<Path> allFiles = Files.list(locationFolder)
                    .filter(Files::isRegularFile)
                    .sorted(Comparator.comparing(this::extractMjdFromFilename).reversed())
                    .limit(4)
                    .collect(Collectors.toList());

            for (Path filePath : allFiles) {
                FileInfo fileInfo = extractSourceAndMJD(filePath.getFileName().toString());
                if (fileInfo != null) {
                    processLiveFile(filePath, fileInfo.source, fileInfo.mjd);
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
            repository.save(data);

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
        return repository.findSessionCountsByFilters(source, mjd, currentSessionCount, expectedSessionCount);
    }

    public List<SourceSessionStatusDTO> getSessionCompleteness(String mjd) {
        return repository.findSessionCountsByMjd(mjd);
    }
}
