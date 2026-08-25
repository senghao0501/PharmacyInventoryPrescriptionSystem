package pharmacy.repository;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class TxtDataStore {
    private final String dataDirectory = "data";

    public TxtDataStore() {
        File directory = new File(dataDirectory);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    public List<String> readLines(String fileName) {
        List<String> lines = new ArrayList<>();
        File file = new File(dataDirectory + File.separator + fileName);
        if (!file.exists()) {
            return lines;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to read file: " + fileName, e);
        }
        return lines;
    }

    public void overwrite(String fileName, List<String> lines) {
        File file = new File(dataDirectory + File.separator + fileName);
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            for (String line : lines) {
                writer.println(line);
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to write file: " + fileName, e);
        }
    }

    public void append(String fileName, String line) {
        File file = new File(dataDirectory + File.separator + fileName);
        try (PrintWriter writer = new PrintWriter(new FileWriter(file, true))) {
            writer.println(line);
        } catch (IOException e) {
            throw new RuntimeException("Unable to append to file: " + fileName, e);
        }
    }
}
