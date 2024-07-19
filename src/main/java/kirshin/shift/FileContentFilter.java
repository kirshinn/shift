package kirshin.shift;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class FileContentFilter {
    private static final String INTEGER_FILE = "integers.txt";
    private static final String FLOAT_FILE = "floats.txt";
    private static final String STRING_FILE = "strings.txt";

    private String outputPath = ".";
    private String filePrefix = "";
    private boolean appendMode = false;
    private boolean shortStats = false;
    private boolean fullStats = false;

    private final List<Integer> integers = new ArrayList<>();
    private final List<Double> floats = new ArrayList<>();
    private final List<String> strings = new ArrayList<>();

    public static void main(String[] args) {
        FileContentFilter filter = new FileContentFilter();
        filter.run(args);
    }

    public void run(String[] args) {
        try {
            parseArguments(args);
            processFiles(args);
            writeOutputFiles();
            printStatistics();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private void parseArguments(String[] args) {
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-o" -> outputPath = args[++i];
                case "-p" -> filePrefix = args[++i];
                case "-a" -> appendMode = true;
                case "-s" -> shortStats = true;
                case "-f" -> fullStats = true;
                default -> {
                    if (args[i].startsWith("-")) {
                        throw new IllegalArgumentException("Unknown option: " + args[i]);
                    }
                }
            }
        }
    }

    private void processFiles(String[] args) throws IOException {
        for (String arg : args) {
            if (!arg.startsWith("-") && !arg.equals(outputPath)) {
                processFile(arg);
            }
        }
    }

    private void processFile(String fileName) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                classifyLine(line);
            }
        }
    }

    private void classifyLine(String line) {
        line = line.trim();
        if (line.isEmpty()) {
            return;
        }
        try {
            integers.add(Integer.parseInt(line));
        } catch (NumberFormatException e1) {
            try {
                floats.add(Double.parseDouble(line));
            } catch (NumberFormatException e2) {
                strings.add(line);
            }
        }
    }

    private void writeOutputFiles() throws IOException {
        writeToFile(INTEGER_FILE, integers);
        writeToFile(FLOAT_FILE, floats);
        writeToFile(STRING_FILE, strings);
    }

    private <T> void writeToFile(String fileName, List<T> data) throws IOException {
        if (data.isEmpty()) {
            return;
        }
        Path filePath = Paths.get(outputPath, filePrefix + fileName);
        try (BufferedWriter writer = Files.newBufferedWriter(filePath,
                appendMode ? StandardOpenOption.APPEND : StandardOpenOption.CREATE,
                StandardOpenOption.WRITE)) {
            for (T item : data) {
                writer.write(item.toString());
                writer.newLine();
            }
        }
    }

    private void printStatistics() {
        if (shortStats) {
            printShortStatistics();
        } else if (fullStats) {
            printFullStatistics();
        }
    }

    private void printShortStatistics() {
        System.out.println("Short Statistics:");
        System.out.println("Integers: " + integers.size());
        System.out.println("Floats: " + floats.size());
        System.out.println("Strings: " + strings.size());
    }

    private void printFullStatistics() {
        System.out.println("Full Statistics:");
        printNumberStats("Integers", integers);
        printNumberStats("Floats", floats);
        printStringStats();
    }

    private <T extends Number & Comparable<? super T>> void printNumberStats(String label, List<T> numbers) {
        if (numbers.isEmpty()) {
            return;
        }
        System.out.println(label + ":");
        System.out.println("  Count: " + numbers.size());
        System.out.println("  Min: " + Collections.min(numbers));
        System.out.println("  Max: " + Collections.max(numbers));
        double sum = numbers.stream().mapToDouble(Number::doubleValue).sum();
        System.out.println("  Sum: " + sum);
        System.out.println("  Average: " + (sum / numbers.size()));
    }

    private void printStringStats() {
        if (strings.isEmpty()) {
            return;
        }
        System.out.println("Strings:");
        System.out.println("  Count: " + strings.size());
        int minLength = strings.stream().mapToInt(String::length).min().orElse(0);
        int maxLength = strings.stream().mapToInt(String::length).max().orElse(0);
        System.out.println("  Shortest length: " + minLength);
        System.out.println("  Longest length: " + maxLength);
    }
}