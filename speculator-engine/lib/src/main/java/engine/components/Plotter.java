package engine.components;

import engine.PriceData.Ticker;
import engine.PriceData.TimeSeries;

import java.util.List;

public abstract class Plotter <V extends Number> implements Multiplot<V> {

    public abstract void plot (TimeSeries<V> input, String label);

    public abstract void unplot ();

    @Override
    public void plotAll(
            List<? extends Ticker> tickers,
            List<? extends TimeSeries<V>> featuresLs,
            List<? extends TimeSeries<V>> predsLs
    ) {
        this.plotAll(tickers, featuresLs, predsLs, List.of());
    }

    @Override
    public void plotAll(
            List<? extends Ticker> tickers,
            TimeSeries<V> features,
            List<? extends TimeSeries<V>> predsLs,
            TimeSeries<V> targets
    ) {
        this.plotAll(
                tickers,
                List.of(features),
                predsLs,
                List.of(targets)
        );
    }

    @Override
    public void plotAll(
            List<? extends Ticker> tickers,
            TimeSeries<V> features,
            List<? extends TimeSeries<V>> predsLs
    ) {
        this.plotAll(
                tickers,
                List.of(features),
                predsLs,
                List.of()
        );
    }

    @Override
    public void plotAll(List<? extends Ticker> tickers, List<? extends TimeSeries<V>> featuresLs) {
        this.plotAll(
                tickers,
                featuresLs,
                List.of(),
                List.of()
        );
    }



}
