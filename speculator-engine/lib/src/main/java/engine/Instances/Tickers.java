package engine.Instances;


import static engine.Instances.Upstreams.oanda;

import engine.PriceData.Ticker;
import engine.Util.Pair;
import engine.components.Upstream;
import engine.upstreams.Oanda;

import java.util.List;
import java.util.Map;

public class Tickers {
    //CONFIG
    /// all possible Tickers; including those not currently displayed in menu
    public static Ticker XNG = Ticker.of("XNGUSD", List.of(
            /// in descending order of preference
            Pair.create(oanda, "NATGAS_USD")
    ));

    public static Ticker SGD = Ticker.of("USDSGD", List.of(
            Pair.create(oanda, "USD_SGD")
    ));

    public static Map<String, Ticker> map = Map.of(
            // CONFIG
            XNG.getName(), XNG,
            SGD.getName(), SGD
    );
}
