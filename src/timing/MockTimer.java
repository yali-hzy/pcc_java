package timing;

import java.time.Duration;
import java.time.Instant;

public class MockTimer {
    private final Instant mockStart;
    private final Instant systemStart;

    public MockTimer(Instant mockStart) {
        this.mockStart = mockStart;
        this.systemStart = Instant.now();
    }

    public Instant now() {
        Duration elapsed = Duration.between(systemStart, Instant.now());
        return mockStart.plus(elapsed);
    }
}
