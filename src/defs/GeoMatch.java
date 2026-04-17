package defs;

import java.util.List;

public final class GeoMatch {
    private record Point(double latitude, double longitude) {}

    private GeoMatch() {
    }

    public static boolean inHotArea(String hotArea, double longitude, double latitude) {
        Point p = new Point(latitude, longitude);
        return switch (hotArea) {
            case "A" -> inPolygon(p, List.of(new Point(22.571615, 113.923059), new Point(22.573121, 113.864853), new Point(22.590556, 113.882534), new Point(22.590873, 113.901760)));
            case "B" -> inPolygon(p, List.of(new Point(22.548391, 113.89455), new Point(22.573121, 113.864853), new Point(22.590556, 113.882534), new Point(22.590873, 113.90176)));
            case "C" -> inPolygon(p, List.of(new Point(22.571615, 113.923059), new Point(22.548391, 113.89455), new Point(22.573121, 113.864853), new Point(22.590556, 113.882534), new Point(22.590873, 113.901761)));
            case "D" -> inPolygon(p, List.of(new Point(22.559489, 114.02018), new Point(22.570902, 114.085411), new Point(22.503359, 114.060348)));
            case "E" -> inPolygon(p, List.of(new Point(22.559489, 114.092304), new Point(22.571853, 114.142402), new Point(22.541416, 114.135879), new Point(22.532457, 114.08485)));
            case "F" -> inPolygon(p, List.of(new Point(22.559489, 114.02018), new Point(22.570902, 114.085411), new Point(22.571853, 114.142402), new Point(22.541416, 114.135879), new Point(22.503359, 114.060348)));
            case "G" -> inPolygon(p, List.of(new Point(22.565195, 113.927826), new Point(22.55616, 114.015056), new Point(22.528414, 114.019691), new Point(22.514936, 113.937809), new Point(22.531744, 113.902618)));
            case "H" -> inPolygon(p, List.of(new Point(22.565195, 113.927826), new Point(22.55616, 114.015056), new Point(22.611317, 113.988792)));
            case "I" -> inPolygon(p, List.of(new Point(22.565195, 113.927826), new Point(22.55616, 114.015056), new Point(22.528414, 114.019691), new Point(22.514936, 113.937809), new Point(22.531744, 113.902618)));
            default -> throw new IllegalArgumentException("Unknown hot area: " + hotArea);
        };
    }

    private static boolean inPolygon(Point point, List<Point> polygon) {
        int count = 0;
        for (int i = 0; i < polygon.size(); i++) {
            Point p1 = polygon.get(i);
            Point p2 = polygon.get((i - 1 + polygon.size()) % polygon.size());
            if (((point.latitude >= p1.latitude) && (point.latitude < p2.latitude))
                || ((point.latitude >= p2.latitude) && (point.latitude < p1.latitude))) {
                if (Math.abs(p1.latitude - p2.latitude) > 0.0) {
                    double y = p1.longitude - ((p1.longitude - p2.longitude) * (p1.latitude - point.latitude)) / (p1.latitude - p2.latitude);
                    if (y == point.longitude) {
                        return true;
                    }
                    if (y < point.longitude) {
                        count++;
                    }
                }
            }
        }
        return count % 2 != 0;
    }
}
