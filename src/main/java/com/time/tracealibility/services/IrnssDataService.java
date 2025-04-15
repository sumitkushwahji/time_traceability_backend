package com.time.tracealibility.services;

import com.time.tracealibility.entity.IrnssData;
import com.time.tracealibility.repository.IrnssDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@Service
public class IrnssDataService {

    @Value("${irnss.file1-path}")
    private String file1Path;

    @Value("${irnss.file2-path}")
    private String file2Path;

    @Autowired
    private IrnssDataRepository repository;


    public void processFiles() throws IOException {
        processFile(Paths.get(file1Path), "IRLMB");
        processFile(Paths.get(file2Path), "IRNPLI");
    }

    public void processFile(Path filePath, String sourceLabel) throws IOException {
        List<String> lines = Files.readAllLines(filePath);
        if (lines.size() <= 19) return;

        for (int i = 19; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) {
                System.out.println("Skipping empty line at " + i);
                continue;
            }
            if (!Character.isDigit(line.charAt(0))) {
                System.out.println("Skipping non-data line: " + line);
                continue;
            }

            String[] tokens = line.trim().split("\\s+");
            System.out.println("Line " + i + " token count: " + tokens.length);
            System.out.println("Content: " + Arrays.toString(tokens));

            // Skip lines with fewer than 26 tokens
            if (tokens.length < 25) {
                System.out.println("Skipping line with insufficient tokens: " + Arrays.toString(tokens));
                continue;
            }

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
            data.setFrc(tokens[22]);  // Use String for FRC
            data.setCk(tokens[23]);   // CK is a String (e.g., "LSC")
            data.setIonType(tokens[24]);         // "28"
                     // "DUAL"

            // Assign the source file name correctly
            data.setSource(sourceLabel);

            System.out.println("Saving: " + data);
            System.out.println("Saving entry: SAT = " + data.getSat() + ", MJD = " + data.getMjd());
            repository.save(data);
        }
    }


    private int parseSignedInt(String str) {
        return Integer.parseInt(str.replace("+", ""));
    }
}
