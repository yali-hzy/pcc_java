package parse;

import defs.Pattern;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public final class PatternParser {
    private PatternParser() {
    }

    public static List<Pattern> parsePatternsFromXml(Document xml) {
        List<Pattern> patterns = new ArrayList<>();
        var nodes = xml.getDocumentElement().getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node patternNode = nodes.item(i);
            if (patternNode.getNodeType() != Node.ELEMENT_NODE || !"pattern".equals(patternNode.getNodeName())) {
                continue;
            }
            Pattern pat = new Pattern(patternNode.getAttributes().getNamedItem("name").getNodeValue());
            var children = patternNode.getChildNodes();
            for (int j = 0; j < children.getLength(); j++) {
                Node child = children.item(j);
                if (child.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }
                String value = nodeValueTransform(child.getTextContent().trim());
                switch (child.getNodeName()) {
                    case "category" -> pat.category = value;
                    case "subject" -> pat.subject = value;
                    case "predicate" -> pat.predicate = value;
                    case "object" -> pat.object = value;
                    case "lifespan" -> pat.lifespan = value == null ? null : TimeParser.parseLifespan(value);
                    case "site" -> pat.site = value;
                    case "timestamp" ->
                        pat.timestamp = value == null ? null : TimeParser.parseDateTimeFromTimeStamp(value);
                    default -> throw new IllegalArgumentException("Unknown pattern field: " + child.getNodeName());
                }
            }
            patterns.add(pat);
        }
        return patterns;
    }

    public static List<Pattern> parseTaxiPatternsFromXml(Document xml) {
        List<Pattern> patterns = new ArrayList<>();
        var nodes = xml.getDocumentElement().getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node patternNode = nodes.item(i);
            if (patternNode.getNodeType() != Node.ELEMENT_NODE || !"pattern".equals(patternNode.getNodeName())) {
                continue;
            }
            String name = null;
            String category = null;
            String subject = null;
            String predicate = null;
            String object = null;
            defs.Lifespan lifespan = null;
            String site = null;
            java.time.Instant timestamp = null;
            Duration freshness = null;

            var children = patternNode.getChildNodes();
            for (int j = 0; j < children.getLength(); j++) {
                Node child = children.item(j);
                if (child.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }
                String value = nodeValueTransform(child.getTextContent().trim());
                switch (child.getNodeName()) {
                    case "id" -> name = value;
                    case "freshness" -> freshness = value == null ? null : Duration.ofMillis(Long.parseLong(value));
                    case "category" -> category = value;
                    case "subject" -> subject = value;
                    case "predicate" -> predicate = value;
                    case "object" -> object = value;
                    case "lifespan" -> lifespan = value == null ? null : TimeParser.parseLifespan(value);
                    case "site" -> site = value;
                    case "timestamp" -> timestamp = value == null ? null : TimeParser.parseDateTimeFromTimeStamp(value);
                    default -> throw new IllegalArgumentException("Unknown pattern field: " + child.getNodeName());
                }
            }

            Pattern pat = new Pattern(name);
            pat.category = category;
            pat.subject = subject;
            pat.predicate = predicate;
            pat.object = object;
            pat.lifespan = lifespan;
            pat.site = site;
            pat.timestamp = timestamp;
            pat.freshness = freshness;
            patterns.add(pat);
        }
        return patterns;
    }

    private static String nodeValueTransform(String value) {
        if (value == null || value.isEmpty() || "any".equals(value)) {
            return null;
        }
        return value;
    }
}
