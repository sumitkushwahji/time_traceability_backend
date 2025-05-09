package com.time.tracealibility.services;

import com.time.tracealibility.dto.SourceSessionStatusDTO;
import com.time.tracealibility.entity.IrnssData;
import com.time.tracealibility.repository.IrnssDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class IrnssDataService {

    @Value("${irnss.file1-folder}")
    private String file1Folder;

    @Value("${irnss.file2-folder}")
    private String file2Folder;

    @Autowired
    private IrnssDataRepository repository;

    public void processFiles() throws IOException {
        processFolder(Paths.get(file1Folder));
        processFolder(Paths.get(file2Folder));
    }

    public void processFolder(Path folderPath) throws IOException {
        if (!Files.exists(folderPath) || !Files.isDirectory(folderPath)) {
            System.out.println("Folder does not exist: " + folderPath);
            return;
        }

        List<Path> files = Files.list(folderPath)
                .filter(Files::isRegularFile)
                .sorted(Comparator.comparing(Path::getFileName))
                .collect(Collectors.toList());

        for (Path file : files) {
            String filename = file.getFileName().toString();
            FileInfo fileInfo = extractSourceAndMJD(filename);
            if (fileInfo == null) continue;

            // Skip if data already exists for MJD and source
            boolean exists = repository.existsByMjdAndSource(fileInfo.mjd, fileInfo.source);
            if (!exists) {
                System.out.println("Processing file: " + filename);
                processFile(file, fileInfo.source);
            }
        }
    }

    public void processFile(Path filePath, String sourceLabel) throws IOException {
        List<String> lines = Files.readAllLines(filePath);
        if (lines.size() <= 19) return;

        for (int i = 19; i < lines.size(); i++) {
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
            data.setSource(sourceLabel);

            repository.save(data);
        }
    }

    private int parseSignedInt(String str) {
        return Integer.parseInt(str.replace("+", ""));
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
        return repository.findSessionCountsByFilters(source, mjd, currentSessionCount, expectedSessionCount);
    }

    public List<SourceSessionStatusDTO> getSessionCompleteness(String mjd) {
        return repository.findSessionCountsByMjd(mjd);
    }
}
