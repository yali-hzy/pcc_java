package defs;

import java.time.Duration;
import java.time.Instant;

public class Pattern {
    public final String name;
    public Duration freshness = Common.FRESHNESS;

    public String category;
    public String subject;
    public String predicate;
    public String object;
    public Lifespan lifespan;
    public String site;
    public Instant timestamp;

    public Pattern(String name) {
        this.name = name;
    }

    public boolean matches(Context context) {
        if (category != null && !category.equals(context.category)) {
            return false;
        }
        if (subject != null && !subject.equals(context.subject)) {
            return false;
        }
        if (predicate != null && !predicate.equals(context.predicate)) {
            return false;
        }
        if (object != null && !object.equals(context.object)) {
            return false;
        }
        if (lifespan != null && !lifespan.equals(context.lifespan)) {
            return false;
        }
        if (site != null) {
            if (site.startsWith("hotarea_")) {
                if (!GeoMatch.inHotArea(site.substring(8), context.longitude, context.latitude)) {
                    return false;
                }
            } else if (!site.equals(context.site)) {
                return false;
            }
        }
        if (timestamp != null && !timestamp.equals(context.timestamp)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Pattern(name=" + name
                + ", category=" + category
                + ", subject=" + subject
                + ", predicate=" + predicate
                + ", object=" + object
                + ", lifespan=" + lifespan
                + ", site=" + site
                + ", timestamp=" + timestamp + ")";
    }
}
