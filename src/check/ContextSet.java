package check;

import defs.Context;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import timing.GlobalTimer;

public class ContextSet {
    public static class ContextCounter {
        public final Context context;
        public int count;

        public ContextCounter(Context context, int count) {
            this.context = context;
            this.count = count;
        }
    }

    public final Map<Integer, ContextCounter> contextSet = new HashMap<>();
    private final int id;
    private final CctChecker checker;
    private final Duration freshness;

    public ContextSet(int id, CctChecker checker, Duration freshness) {
        this.id = id;
        this.checker = checker;
        this.freshness = freshness;
    }

    public void add(int contextId, Context context) {
        synchronized (checker.mutex) {
            contextSet.put(contextId, new ContextCounter(context, 0));
            checker.addChangeNum++;
            for (Map.Entry<Integer, java.util.List<Integer>> e : checker.patternMap.getOrDefault(id, Map.of()).entrySet()) {
                contextSet.get(contextId).count++;
                checker.method.handleAdd(checker.ccts.get(e.getKey()), e.getValue(), contextId, context);
            }
        }

        Instant expireAt = context.timestamp.plus(freshness);
        Duration delay = Duration.between(GlobalTimer.get().now(), expireAt);
        long delayMs = Math.max(0L, delay.toMillis());

        checker.scheduler.schedule(() -> {
            synchronized (checker.mutex) {
                ContextCounter removed = contextSet.remove(contextId);
                if (removed == null) {
                    return;
                }
                checker.deleteChangeNum++;
                int count = removed.count;
                for (Map.Entry<Integer, java.util.List<Integer>> e : checker.patternMap.getOrDefault(id, Map.of()).entrySet()) {
                    if (count <= 0) {
                        break;
                    }
                    checker.method.handleDelete(checker.ccts.get(e.getKey()), e.getValue(), contextId);
                    count--;
                }
                if (checker.addChangeNum == checker.deleteChangeNum) {
                    checker.mutex.notifyAll();
                }
            }
        }, delayMs, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    public void offlineAdd(int contextId, Context context) {
        contextSet.put(contextId, new ContextCounter(context, 0));
        for (Map.Entry<Integer, java.util.List<Integer>> e : checker.patternMap.getOrDefault(id, Map.of()).entrySet()) {
            checker.method.handleAdd(checker.ccts.get(e.getKey()), e.getValue(), contextId, context);
        }
    }

    public void offlineDelete(int contextId) {
        contextSet.remove(contextId);
        for (Map.Entry<Integer, java.util.List<Integer>> e : checker.patternMap.getOrDefault(id, Map.of()).entrySet()) {
            checker.method.handleDelete(checker.ccts.get(e.getKey()), e.getValue(), contextId);
        }
    }
}
