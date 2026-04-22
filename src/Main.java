import collection.ContextFlow;
import check.CctChecker;
import defs.Common;
import defs.Context;
import logger.GlobalLogger;
import logger.Logger;
import parse.DataLoader;
import parse.TimeParser;
import timing.GlobalTimer;
import timing.MockTimer;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Main {
    public static void main(String[] args) {
        Map<String, String> options = parseArgs(args);

        String dataPath = options.get("data");
        String convert = options.get("convert");
        String method = options.get("method");
        int freshness = Integer.parseInt(options.get("freshness"));
        int interval = Integer.parseInt(options.get("interval"));
        String outfile = options.getOrDefault("outfile",
                "context_check_" + convert + "_" + method + "_" + freshness + "_" + interval + ".log");

        GlobalLogger.set(new Logger(options.get("outdir"), outfile));
        Common.FRESHNESS = Duration.ofSeconds(freshness);

        try {
            DataLoader.DataBundle bundle;
            if (options.containsKey("offline")) {
                bundle = DataLoader.loadTaxiData(dataPath, options.get("context"), options.get("pattern"),
                        options.get("constraint"));
            } else {
                bundle = DataLoader.loadData(dataPath);
            }

            CctChecker checker = new CctChecker(bundle.patterns());
            checker.initialize(bundle.constraints(), convert);
            if (options.containsKey("offline")) {
                checker.checkOffline(bundle.contexts(), method);
            } else {
                Instant startTime = options.containsKey("start_time")
                        ? TimeParser.parseDateTimeFromTimeStamp(options.get("start_time"))
                        : bundle.contexts().get(0).timestamp;
                GlobalTimer.set(new MockTimer(startTime));

                BlockingQueue<Context> queue = new LinkedBlockingQueue<>();
                ContextFlow flow = new ContextFlow(Duration.ofMillis(interval), queue, bundle.contexts().iterator());
                checker.checkOnline(flow, method);
            }
        } finally {
            GlobalLogger.get().close();
        }
    }

    private static Map<String, String> parseArgs(String[] args) {
        Map<String, String> map = new HashMap<>();
        map.put("context", "context.txt");
        map.put("pattern", "patterns.xml");
        map.put("constraint", "constraints.xml");
        map.put("convert", "kernel");
        map.put("method", "ecc");
        map.put("freshness", "20");
        map.put("outdir", "log");
        map.put("interval", "400");

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (!arg.startsWith("--")) {
                continue;
            }
            String key = arg.substring(2);
            if ("offline".equals(key)) {
                map.put("offline", "true");
                continue;
            }
            if (i + 1 >= args.length) {
                throw new IllegalArgumentException("Missing value for option: " + arg);
            }
            map.put(key, args[++i]);
        }

        if (!map.containsKey("data")) {
            throw new IllegalArgumentException("--data is required");
        }
        return map;
    }
}
