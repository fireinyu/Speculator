package engine.components;

import engine.PriceData.Ticker;
import engine.PriceData.TimeSeries;

import java.util.List;

public interface Multiplot <V extends Number>{

    public abstract void plotAll(
            List<? extends Ticker> tickers,
            List<? extends TimeSeries<V>> featuresLs,
            List<? extends TimeSeries<V>> predsLs,
            List<? extends TimeSeries<V>> targetsLs
    );

    void plotAll(
            List<? extends Ticker> tickers,
            List<? extends TimeSeries<V>> featuresLs,
            List<? extends TimeSeries<V>> predsLs
    );

    void plotAll(
            List<? extends Ticker> tickers,
            TimeSeries<V> features,
            List<? extends TimeSeries<V>> predsLs,
            TimeSeries<V> targets
    );

    void plotAll(
            List<? extends Ticker> tickers,
            TimeSeries<V> features,
            List<? extends TimeSeries<V>> predsLs
    );

    void plotAll(List<? extends Ticker> tickers, List<? extends TimeSeries<V>> featuresLs);
}
