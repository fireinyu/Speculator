package engine.Instances;

import engine.components.Upstream;
import engine.upstreams.Oanda;

public class Upstreams {
    public static Upstream<Float> oanda = new Oanda();
}
