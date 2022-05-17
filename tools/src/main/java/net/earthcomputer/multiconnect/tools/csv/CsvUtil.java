package net.earthcomputer.multiconnect.tools.csv;

import net.earthcomputer.multiconnect.tools.Util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class CsvUtil {
    private CsvUtil() {}

    public static void readCsv(Path file, CsvVisitor visitor) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(file)) {
            boolean readHeader = false;
            String line;
            while ((line = reader.readLine()) != null) {
                String actualContent = Util.substringBefore(line, '#');
                if (!actualContent.isBlank()) {
                    String[] parts = actualContent.trim().split(" ");
                    List<String> partsList = Arrays.asList(parts);
                    if (!readHeader) {
                        visitor.visitHeader(partsList);
                        readHeader = true;
                    } else {
                        visitor.visitRow(partsList);
                    }
                }
            }
        }
    }

    public static void modifyCsv(Path file, CsvVisitor visitor) throws IOException {
        Path swapFile = Files.createTempFile("multiconnect.", ".csv");
        try {
            try (BufferedReader reader = Files.newBufferedReader(file);
                 BufferedWriter writer = Files.newBufferedWriter(swapFile)
            ) {
                boolean readHeader = false;
                String line;
                while ((line = reader.readLine()) != null) {
                    String actualContent = Util.substringBefore(line, '#');
                    if (!actualContent.isBlank()) {
                        String[] parts = actualContent.trim().split(" ");
                        List<String> partsList = new ArrayList<>(parts.length);
                        Collections.addAll(partsList, parts);
                        if (!readHeader) {
                            visitor.visitHeader(partsList);
                            readHeader = true;
                        } else {
                            visitor.visitRow(partsList);
                        }
                        String resultingLine = String.join(" ", partsList) + line.substring(actualContent.length()) + System.lineSeparator();
                        writer.write(resultingLine);
                    } else {
                        writer.write(line + System.lineSeparator());
                    }
                }
            }
            Files.move(swapFile, file, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(swapFile);
        }
    }

    public static void set(List<String> row, int index, String value) {
        if (index >= 0) {
            while (row.size() <= index) {
                row.add("null");
            }
            row.set(index, value);
        }
    }
}
