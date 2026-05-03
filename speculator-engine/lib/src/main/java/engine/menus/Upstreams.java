package engine.menus;

import java.util.List;

import engine.PriceData.Upstream;
import engine.Serialisation.Menu;
import engine.components.Ticker;
import engine.upstreams.Oanda;

public class Upstreams {
    public static Upstream oanda = new Oanda(0);
    public static List<Upstream> list = List.of(
            /// CONFIG
            oanda
    );
    public static Menu<Upstream> menu = new Menu<>(list);

}
