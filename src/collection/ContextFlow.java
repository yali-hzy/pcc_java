package collection;

import defs.Context;
import java.time.Duration;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ContextFlow {
    public final BlockingQueue<Context> queue;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public ContextFlow(Duration interval, BlockingQueue<Context> queue, Iterator<Context> contextsIt) {
        this.queue = queue;
        long millis = Math.max(1L, interval.toMillis());
        scheduler.scheduleAtFixedRate(() -> {
            try {
                if (contextsIt.hasNext()) {
                    queue.put(contextsIt.next());
                } else {
                    queue.put(Context.POISON_CONTEXT);
                    cancel();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, 0L, millis, TimeUnit.MILLISECONDS);
    }

    public void cancel() {
        scheduler.shutdownNow();
    }
}
