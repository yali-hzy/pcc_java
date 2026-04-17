package timing;

public final class GlobalTimer {
    private static MockTimer timer;

    private GlobalTimer() {
    }

    public static void set(MockTimer t) {
        timer = t;
    }

    public static MockTimer get() {
        if (timer == null) {
            throw new IllegalStateException("Global timer is not initialized");
        }
        return timer;
    }
}
