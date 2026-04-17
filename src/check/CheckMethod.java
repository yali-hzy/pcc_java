package check;

import defs.Context;
import java.util.List;

public interface CheckMethod {
    void handleAdd(Cct cct, List<Integer> nodeList, int contextId, Context context);

    void handleDelete(Cct cct, List<Integer> nodeList, int contextId);
}
