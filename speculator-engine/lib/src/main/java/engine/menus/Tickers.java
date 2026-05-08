package engine.menus;


import static engine.menus.Upstreams.oanda;
import static engine.menus.Upstreams.random;

import engine.Serialisation.Menu;
import engine.components.Ticker;
import engine.Util.Pair;

import java.util.List;

public class Tickers {
    /// CONFIG
    /// all possible Tickers; including those not currently displayed in menu
    public static Ticker XNG = Ticker.of("XNGUSD", List.of(
            /// in descending order of preference
            Pair.create(oanda, "NATGAS_USD"),
            Pair.create(random, "xng")
    ),0);

    public static Ticker SGD = Ticker.of("USDSGD", List.of(
            Pair.create(oanda, "USD_SGD"),
            Pair.create(random, "sgd")
    ),1);

    public static List<Ticker> list = List.of(
            /// CONFIG
            XNG,
            SGD
    );
    public static Menu<Ticker> menu = new Menu<>(list);
}
