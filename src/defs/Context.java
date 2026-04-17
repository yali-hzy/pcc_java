package defs;

import java.time.Instant;

public class Context implements Comparable<Context> {
    public static final String POISON_STRING = "POISON";
    public static final Instant POISON_INSTANT = Instant.EPOCH.minusSeconds(5000L * 365L * 24L * 3600L);
    public static final Context POISON_CONTEXT = new Context();

    public String category = POISON_STRING;
    public String subject = POISON_STRING;
    public String predicate = POISON_STRING;
    public String object = POISON_STRING;
    public Lifespan lifespan = new Lifespan.Moment(POISON_INSTANT);
    public String site = POISON_STRING;
    public Instant timestamp = POISON_INSTANT;
    public String id = POISON_STRING;
    public double longitude = 0.0;
    public double latitude = 0.0;
    public double speed = 0.0;

    @Override
    public int compareTo(Context other) {
        return this.timestamp.compareTo(other.timestamp);
    }

    @Override
    public String toString() {
        if (this == POISON_CONTEXT) {
            return "POISON_CONTEXT";
        }
        return "Context(\n"
            + "\tcategory=" + category + "\n"
            + "\tsubject=" + subject + "\n"
            + "\tpredicate=" + predicate + "\n"
            + "\tobject=" + object + "\n"
            + "\tsite=" + site + "\n"
            + "\tlongitude=" + longitude + "\n"
            + "\tlatitude=" + latitude + "\n"
            + "\tspeed=" + speed + "\n"
            + "\tlifespan=" + lifespan + "\n"
            + "\ttimestamp=" + timestamp + "\n"
            + ")";
    }
}
