package engine.upstreams;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;

import engine.PriceData.Candle;
import engine.PriceData.NAVPosition;
import engine.PriceData.TimeSeries;
import engine.PriceData.Upstream;
import engine.components.Ticker;

public class RandomUpstream extends Upstream {
    private Random rng = new Random(0);

    public RandomUpstream(int index) {
        super(index);
    }
    @Override
    public HashMap<Ticker, NAVPosition> fetchPositionsNow(Set<Ticker> tickers) {
        return new HashMap<>();
    }
    @Override
    protected TimeSeries fetchCountUntilAtLeast(Ticker ticker, Duration interval, int leftDependency, ZonedDateTime until) {
        List<Candle> candles = new ArrayList<>();
        for (ZonedDateTime at = until.minus(interval.multipliedBy(leftDependency)); !at.isAfter(until); at = at.plus(interval)) {
            candles.add(new Candle(at, rng.nextFloat()));
        }
        return new TimeSeries(candles);
    }

    @Override
    public String toString() {
        return "Test upstream (random)";
    }

    @Override
    protected TimeSeries fetchBetweenAtLeast(Ticker ticker, Duration interval, ZonedDateTime from, ZonedDateTime to) {
        List<Candle> candles = new ArrayList<>();
        for (ZonedDateTime at = from; !at.isAfter(to); at = at.plus(interval)) {
            candles.add(new Candle(at, rng.nextFloat()));
        }
        return new TimeSeries(candles);
    }
}
