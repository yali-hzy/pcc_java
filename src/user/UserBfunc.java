package user;

import defs.Context;

public final class UserBfunc {
    private UserBfunc() {
    }

    public static boolean withinTrans(Context ctx1, Context ctx2) {
        return Math.abs(ctx1.timestamp.getEpochSecond() - ctx2.timestamp.getEpochSecond()) <= 5 * 60;
    }

    public static boolean taxiBfunc(String name, Context[] args) {
        return switch (name) {
            case "same" -> same(args[0], args[1]);
            case "sz_loc_range" -> szLocRange(args[0]);
            case "sz_loc_close" -> szLocClose(args[0], args[1]);
            case "sz_spd_close" -> szSpdClose(args[0], args[1]);
            case "sz_loc_dist" -> szLocDist(args[0], args[1]);
            default -> throw new IllegalArgumentException("Unknown Bfunc name: " + name);
        };
    }

    public static boolean same(Context ctx1, Context ctx2) {
        return ctx1.subject.equals(ctx2.subject);
    }

    public static boolean szLocRange(Context ctx) {
        double longitude = ctx.longitude;
        double latitude = ctx.latitude;
        return !(longitude < 112.0) && !(longitude > 116.0) && !(latitude < 20.0) && !(latitude > 24.0);
    }

    public static boolean szLocClose(Context ctx1, Context ctx2) {
        double lon1 = ctx1.longitude;
        double lat1 = ctx1.latitude;
        double lon2 = ctx2.longitude;
        double lat2 = ctx2.latitude;
        double distSqr = (lon1 - lon2) * (lon1 - lon2) + (lat1 - lat2) * (lat1 - lat2);
        return !(distSqr > 0.001 * 0.001);
    }

    public static boolean szSpdClose(Context ctx1, Context ctx2) {
        return Math.abs(ctx2.speed - ctx1.speed) <= 50.0;
    }

    public static boolean szLocDist(Context ctx1, Context ctx2) {
        double lon1 = ctx1.longitude;
        double lat1 = ctx1.latitude;
        double lon2 = ctx2.longitude;
        double lat2 = ctx2.latitude;
        double distSqr = (lon1 - lon2) * (lon1 - lon2) + (lat1 - lat2) * (lat1 - lat2);
        return !(distSqr > 0.025 * 0.025);
    }
}
