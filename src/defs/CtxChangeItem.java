package defs;

import java.time.Instant;

public class CtxChangeItem implements Comparable<CtxChangeItem> {
    public final int contextId;
    public final Context context;
    public final int patternId;
    public final ContextChangeType changeType;
    public final Instant timestamp;

    public CtxChangeItem(int contextId, Context context, int patternId, ContextChangeType changeType,
            Instant timestamp) {
        this.contextId = contextId;
        this.context = context;
        this.patternId = patternId;
        this.changeType = changeType;
        this.timestamp = timestamp;
    }

    @Override
    public int compareTo(CtxChangeItem other) {
        return this.timestamp.compareTo(other.timestamp);
    }
}
