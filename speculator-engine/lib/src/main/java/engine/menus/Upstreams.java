package engine.menus;

import java.util.List;

import engine.PriceData.Upstream;
import engine.Serialisation.Menu;
import engine.components.Ticker;
import engine.upstreams.Oanda;
import engine.upstreams.RandomUpstream;

public class Upstreams {
    /// CONFIG
    public static Upstream oanda = new Oanda(0);
    public static Upstream random = new RandomUpstream(1);
    public static List<Upstream> list = List.of(
            /// CONFIG
            oanda,
            random
    );
    public static Menu<Upstream> menu = new Menu<>(list);

}
