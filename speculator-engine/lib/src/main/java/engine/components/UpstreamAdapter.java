package engine.components;

import java.time.Duration;

public abstract class UpstreamAdapter {

    public abstract <T extends Upstream & Snapshottable> T makeLeftFor(Duration interval, int leftDependency);

    public abstract Snapshottable makeRightFor(Duration interval, int rightDependency);
}
