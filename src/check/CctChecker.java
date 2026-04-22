package check;

import collection.ContextFlow;
import defs.Constraint;
import defs.Context;
import defs.ContextChangeType;
import defs.CtxChangeItem;
import defs.Pattern;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import logger.GlobalLogger;

public class CctChecker {
    public final Map<String, Integer> patternNames2Id = new HashMap<>();
    public final Map<String, Integer> constraintNames2Id = new HashMap<>();
    public final Map<Integer, Map<Integer, List<Integer>>> patternMap = new HashMap<>();

    public final List<Pattern> patterns;
    public List<Cct> ccts = new ArrayList<>();
    public List<ContextSet> contextSets = new ArrayList<>();

    public int addChangeNum = 0;
    public int deleteChangeNum = 0;
    public CheckMethod method;
    public final Object mutex = new Object();
    public final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public CctChecker(List<Pattern> patterns) {
        this.patterns = patterns;
    }

    public void initialize(List<Constraint> constraints, String convert) {
        validateName(patterns, constraints);
        List<Constraint> converted = new ArrayList<>();
        for (Constraint c : constraints) {
            switch (convert) {
                case "kernel" -> converted.add(new Constraint(c.name(), Convert.kernelConvert(c.formula())));
                case "notleaf" -> converted.add(new Constraint(c.name(), Convert.notLeafConvert(c.formula())));
                case "none" -> converted.add(c);
                default -> throw new IllegalArgumentException("Unsupported convert method: " + convert);
            }
        }

        SyntaxTree.Builder builder = new SyntaxTree.Builder(patternNames2Id, patternMap);
        ccts.clear();
        for (int i = 0; i < converted.size(); i++) {
            ccts.add(new Cct(builder.build(i, converted.get(i))));
        }
    }

    public void checkOffline(List<Context> contexts, String methodName) {
        contextSets = new ArrayList<>(patterns.size());
        for (int i = 0; i < patterns.size(); i++) {
            Pattern pattern = patterns.get(i);
            contextSets.add(new ContextSet(i, this, pattern.freshness));
        }

        method = switch (methodName) {
            case "ecc" -> new Ecc.EccMethod(this);
            case "pcc" -> new Pcc.PccMethod(this);
            default -> throw new IllegalArgumentException("Unsupported check method: " + methodName);
        };

        List<CtxChangeItem> changes = new ArrayList<>();
        for (int num = 0; num < contexts.size(); num++) {
            Context context = contexts.get(num);
            context.id = Integer.toString(num + 1);
            for (int i = 0; i < patterns.size(); i++) {
                Pattern pattern = patterns.get(i);
                if (pattern.matches(context)) {
                    changes.add(new CtxChangeItem(num, context, i, ContextChangeType.ADD, context.timestamp));
                    changes.add(new CtxChangeItem(num, context, i, ContextChangeType.DELETE,
                            context.timestamp.plus(pattern.freshness)));
                }
            }
        }

        Collections.sort(changes);
        GlobalLogger.get().start();
        for (CtxChangeItem item : changes) {
            if (item.changeType == ContextChangeType.ADD) {
                contextSets.get(item.patternId).offlineAdd(item.contextId, item.context);
            } else {
                contextSets.get(item.patternId).offlineDelete(item.contextId);
            }
        }
    }

    public void checkOnline(ContextFlow contexts, String methodName) {
        contextSets = new ArrayList<>(patterns.size());
        for (int i = 0; i < patterns.size(); i++) {
            Pattern pattern = patterns.get(i);
            contextSets.add(new ContextSet(i, this, pattern.freshness));
        }

        method = switch (methodName) {
            case "ecc" -> new Ecc.EccMethod(this);
            case "pcc" -> new Pcc.PccMethod(this);
            default -> throw new IllegalArgumentException("Unsupported check method: " + methodName);
        };

        int n = 0;
        try {
            while (true) {
                Context context = contexts.queue.take();
                if (context == Context.POISON_CONTEXT) {
                    contexts.cancel();
                    synchronized (mutex) {
                        while (addChangeNum != deleteChangeNum) {
                            mutex.wait();
                        }
                    }
                    break;
                }
                n++;
                for (int i = 0; i < patterns.size(); i++) {
                    if (patterns.get(i).matches(context)) {
                        contextSets.get(i).add(n, context);
                    }
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Online check interrupted", e);
        } finally {
            scheduler.shutdownNow();
        }
    }

    private void validateName(List<Pattern> patterns, List<Constraint> constraints) {
        patternNames2Id.clear();
        constraintNames2Id.clear();
        for (int i = 0; i < patterns.size(); i++) {
            String name = patterns.get(i).name;
            if (patternNames2Id.containsKey(name)) {
                throw new IllegalArgumentException("Duplicate pattern name: " + name);
            }
            patternNames2Id.put(name, i);
        }
        for (int i = 0; i < constraints.size(); i++) {
            String name = constraints.get(i).name();
            if (constraintNames2Id.containsKey(name)) {
                throw new IllegalArgumentException("Duplicate constraint name: " + name);
            }
            constraintNames2Id.put(name, i);
        }
    }
}
