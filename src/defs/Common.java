package defs;

import java.time.Duration;

public final class Common {
    public static Duration FRESHNESS = Duration.ofSeconds(20);
    public static boolean DEBUG = false;
    public static long maxUsedHeapSize = 0L;
    public static long maxAllocatedHeapSize = 0L;

    private Common() {
    }
}
