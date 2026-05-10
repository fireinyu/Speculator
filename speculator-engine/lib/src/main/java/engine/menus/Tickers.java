package engine.menus;


import static engine.menus.Upstreams.oanda;
import static engine.menus.Upstreams.random;

import engine.Serialisation.Menu;
import engine.components.Ticker;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Tickers {
    /// CONFIG
    /// all possible Tickers; including those not currently displayed in menu
    private static Ticker.TradingSchedule oandaSchedule = new Ticker.TradingSchedule(
            new Ticker.TradingSession(LocalTime.of(7,0), LocalTime.of(6,0)),
            Map.of(
                    DayOfWeek.MONDAY, new Ticker.TradingSession(LocalTime.of(7,0), LocalTime.MAX),
                    DayOfWeek.SATURDAY, new Ticker.TradingSession(LocalTime.MIN, LocalTime.of(6, 0)),
                    DayOfWeek.SUNDAY, Ticker.TradingSession.empty()
            ),
            new HashMap<>()
    );
    private static Ticker.TradingSchedule fullSchedule = new Ticker.TradingSchedule(
            new Ticker.TradingSession(LocalTime.MIN, LocalTime.MAX),
            Map.of(),
            Map.of()
    );
    public static Ticker XNG = Ticker.of("XNGUSD", List.of(
            /// in descending order of preference
            new Ticker.TickerSource("NATGAS_USD", oandaSchedule, oanda),
            new Ticker.TickerSource("NATGAS_USD", fullSchedule, random)
    ),0);

    public static Ticker SGD = Ticker.of("USDSGD", List.of(
            new Ticker.TickerSource("USD_SGD", oandaSchedule, oanda),
            new Ticker.TickerSource("USD_SGD", fullSchedule, random)
    ),1);

    public static List<Ticker> list = List.of(
            /// CONFIG
            XNG,
            SGD
    );
    public static Menu<Ticker> menu = new Menu<>(list);
}
