package com.time.tracealibility.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "processed_files")
public class ProcessedFile {

    @Id
    private String filePath;

    private int lastLineProcessed;

    public ProcessedFile() {}

    public ProcessedFile(String fileKey, int currentLine) {
        this.filePath = fileKey;
        this.lastLineProcessed = currentLine;
    }


    // Getters and setters
    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public int getLastLineProcessed() {
        return lastLineProcessed;
    }

    public void setLastLineProcessed(int lastLineProcessed) {
        this.lastLineProcessed = lastLineProcessed;
    }

}
