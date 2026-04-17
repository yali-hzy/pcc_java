package logger;

public final class GlobalLogger {
    private static Logger logger;

    private GlobalLogger() {
    }

    public static void set(Logger l) {
        logger = l;
    }

    public static Logger get() {
        if (logger == null) {
            throw new IllegalStateException("Logger is not initialized");
        }
        return logger;
    }
}
