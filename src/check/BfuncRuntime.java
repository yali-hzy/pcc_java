package check;

import defs.Context;
import user.UserBfunc;

public final class BfuncRuntime {
    private BfuncRuntime() {
    }

    public static boolean apply(String name, Context[] args) {
        if ("WithinTrans".equals(name)) {
            return UserBfunc.withinTrans(args[0], args[1]);
        }
        return UserBfunc.taxiBfunc(name, args);
    }
}
