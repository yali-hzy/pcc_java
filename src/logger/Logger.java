package logger;

import defs.Common;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

public class Logger implements AutoCloseable {
    private final BufferedWriter writer;
    private final Set<String> linkSet = new HashSet<>();
    private Duration elapsedTime = Duration.ZERO;

    public Logger(String dir, String fileName) {
        try {
            Path logDir = Path.of(dir).normalize();
            Path logFilePath = logDir.resolve(fileName);
            Files.createDirectories(logDir);
            Files.deleteIfExists(logFilePath);
            this.writer = Files.newBufferedWriter(logFilePath, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize logger", e);
        }
    }

    public synchronized void start() {
    }

    public synchronized void log(String message) {
        if (message == null || message.isEmpty() || linkSet.contains(message)) {
            return;
        }
        try {
            writer.write(message);
            writer.newLine();
            writer.flush();
            System.out.println(message);
            linkSet.add(message);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write log", e);
        }
    }

    public synchronized void addDuration(Duration duration) {
        elapsedTime = elapsedTime.plus(duration);
    }

    @Override
    public synchronized void close() {
        try {
            writer.flush();
            writer.close();
            System.out.println("Elapsed time: " + elapsedTime);
            System.out.println("maxUsedHeapSize: " + Common.maxUsedHeapSize);
            System.out.println("maxAllocatedHeapSize: " + Common.maxAllocatedHeapSize);
        } catch (IOException e) {
            throw new RuntimeException("Failed to close logger", e);
        }
    }
}
