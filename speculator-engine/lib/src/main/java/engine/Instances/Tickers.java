package engine.Instances;


import engine.PriceData.Ticker;
import engine.Util.Pair;
import engine.upstreams.Oanda;

import java.util.List;

public class Tickers {
    //CONFIG
    /// all possible Tickers; including those not currently displayed in menu
    public static Ticker XNG = Ticker.of("XNGUSD", List.of(
            /// in descending order of preference
            Pair.create(Oanda.class, "NATGAS_USD")
    ));

    public static Ticker SGD = Ticker.of("USDSGD", List.of(
            Pair.create(Oanda.class, "USD_SGD")
    ));
}
