package parse;

import defs.Constraint;
import defs.Context;
import defs.Pattern;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class DataLoader {
    public record DataBundle(List<Context> contexts, List<Pattern> patterns, List<Constraint> constraints) {}

    private DataLoader() {
    }

    public static DataBundle loadData(String path) {
        Path base = Path.of(path);
        Path contextsPath = base.resolve("contexts.txt");
        Path patternsPath = base.resolve("patterns.xml");
        Path constraintsPath = base.resolve("constraints.xml");

        List<Context> contexts = new ArrayList<>();
        try {
            for (String line : Files.readAllLines(contextsPath, StandardCharsets.UTF_8)) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                String[] parts = line.split(", ");
                Context ctx = new Context();
                ctx.category = parts[0];
                ctx.subject = parts[1];
                ctx.predicate = parts[2];
                ctx.object = parts[3];
                ctx.lifespan = TimeParser.parseLifespan(parts[4]);
                ctx.site = parts[5];
                ctx.timestamp = TimeParser.parseDateTimeFromTimeStamp(parts[6]);
                contexts.add(ctx);
            }

            var patternsDoc = XmlParser.parseFromString(Files.readString(patternsPath, StandardCharsets.UTF_8));
            var constraintsDoc = XmlParser.parseFromString(Files.readString(constraintsPath, StandardCharsets.UTF_8));
            List<Pattern> patterns = PatternParser.parsePatternsFromXml(patternsDoc);
            List<Constraint> constraints = ConstraintParser.parseConstraintsFromXml(constraintsDoc);
            return new DataBundle(contexts, patterns, constraints);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load data from " + path, e);
        }
    }

    public static DataBundle loadTaxiData(String path, String contextPath, String patternPath, String constraintPath) {
        Path base = Path.of(path);
        Path contextsPath = base.resolve(contextPath);
        Path patternsPath = base.resolve(patternPath);
        Path constraintsPath = base.resolve(constraintPath);

        List<Context> contexts = new ArrayList<>();
        try {
            for (String line : Files.readAllLines(contextsPath, StandardCharsets.UTF_8)) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                String[] parts = line.split(",");
                Context ctx = new Context();
                ctx.category = "location";
                ctx.timestamp = TimeParser.parseDateTimeFromStr(parts[0]);
                ctx.subject = parts[1];
                ctx.longitude = Double.parseDouble(parts[2]);
                ctx.latitude = Double.parseDouble(parts[3]);
                ctx.speed = Double.parseDouble(parts[4]);
                int status = Integer.parseInt(parts[6]);
                ctx.predicate = status == 0 ? "run_without_service" : "run_with_service";
                String subject = parts[1];
                char suffix = subject.charAt(subject.length() - 2);
                ctx.site = "sutpc_" + suffix;
                contexts.add(ctx);
            }

            var patternsDoc = XmlParser.parseFromString(Files.readString(patternsPath, StandardCharsets.UTF_8));
            var constraintsDoc = XmlParser.parseFromString(Files.readString(constraintsPath, StandardCharsets.UTF_8));
            List<Pattern> patterns = PatternParser.parseTaxiPatternsFromXml(patternsDoc);
            List<Constraint> constraints = ConstraintParser.parseTaxiConstraintsFromXml(constraintsDoc);
            return new DataBundle(contexts, patterns, constraints);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load taxi data", e);
        }
    }
}
