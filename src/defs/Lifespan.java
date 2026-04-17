package defs;

import java.time.Instant;

public sealed interface Lifespan permits Lifespan.Moment, Lifespan.Period {
    record Moment(Instant time) implements Lifespan {
        @Override
        public String toString() {
            return "At " + time;
        }
    }

    record Period(Instant start, Instant end) implements Lifespan {
        @Override
        public String toString() {
            return "From " + start + " to " + end;
        }
    }
}
