package engine.Instances;

import engine.components.UpstreamAdapter;
import engine.PriceData.Ticker;
import engine.upstreams.Oanda;

import java.util.List;
import java.util.Map;

public class UpstreamAdapters {
    private static Map<? extends Ticker, UpstreamAdapter> adapters = Map.of(
            //CONFIG
            /// these are the tickers to be displayed in menu
            Tickers.XNG, new Oanda.Adapter(),
            Tickers.SGD, new Oanda.Adapter()
    );

    public static UpstreamAdapter getAdapterFor(Ticker ticker) {
        return adapters.get(ticker);
    }

    public static List<Ticker> getTickers() {
        return List.copyOf(adapters.keySet());
    }
}
