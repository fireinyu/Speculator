package engine.Instances;

import engine.PriceData.Upstream;
import engine.upstreams.Oanda;

public class Upstreams {
    public static Upstream<Float> oanda = new Oanda();
}
